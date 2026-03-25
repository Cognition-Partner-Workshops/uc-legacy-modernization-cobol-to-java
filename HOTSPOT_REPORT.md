# CardDemo Hotspot Report

> Top 10 modules prioritized by code complexity, migration risk, and business impact. Use this report to sequence modernization waves and allocate resources.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
3. [Detailed Hotspot Analysis](#detailed-hotspot-analysis)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Waves](#recommended-migration-waves)
6. [Quick Wins (Low Complexity / High Value)](#quick-wins)

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale), then combined into a weighted composite score:

| Dimension          | Weight | Criteria                                                                                          |
|--------------------|--------|---------------------------------------------------------------------------------------------------|
| **Complexity**     | 40%    | Lines of code, cyclomatic indicators (IF/EVALUATE count), PERFORM nesting, number of copybooks, CICS commands, data files accessed |
| **Risk**           | 30%    | Number of downstream dependents, data integrity impact, PII handling, financial calculations, error handling patterns, ALTER usage |
| **Business Impact**| 30%    | Revenue criticality, user-facing importance, batch cycle centrality, regulatory/audit relevance    |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.4) + (Business Impact x 0.4) -- normalized to 10-point scale.

### Complexity Metrics Used

| Metric              | Description                                      | High Threshold |
|---------------------|--------------------------------------------------|----------------|
| LOC                 | Lines of code                                    | > 700          |
| IF Count            | Number of IF statements (branching complexity)   | > 50           |
| EVALUATE Count      | Number of EVALUATE statements (switch logic)     | > 5            |
| PERFORM Count       | Number of PERFORM statements (call depth)        | > 40           |
| CICS Commands       | Number of EXEC CICS calls                        | > 10           |
| Copybooks Included  | Number of COPY statements                        | > 10           |
| CALL Statements     | Number of CALL/LINK to other programs             | > 2            |
| Files Accessed      | Number of VSAM files read/written                | > 3            |

---

## Top 10 Hotspot Rankings

| Rank | Program    | LOC   | Type    | Complexity | Risk | Business Impact | Composite | Primary Concern                          |
|------|------------|-------|---------|------------|------|-----------------|-----------|------------------------------------------|
| 1    | COACTUPC   | 4,236 | Online  | 10         | 10   | 9               | **9.7**   | Largest program; edits account + customer data |
| 2    | CBTRN02C   | 731   | Batch   | 8          | 10   | 10              | **9.3**   | Core transaction posting; financial integrity  |
| 3    | CBACT04C   | 652   | Batch   | 8          | 9    | 10              | **9.0**   | Interest calculation; financial accuracy       |
| 4    | COCRDLIC   | 1,459 | Online  | 9          | 7    | 8               | **8.0**   | Complex list paging; heavy CICS browsing       |
| 5    | COCRDUPC   | 1,560 | Online  | 9          | 8    | 7               | **8.0**   | Card update with extensive validation          |
| 6    | CBSTM03A   | 924   | Batch   | 8          | 7    | 8               | **7.7**   | Multi-file statement gen; ALTER usage          |
| 7    | CBTRN03C   | 649   | Batch   | 8          | 6    | 8               | **7.3**   | Report generation with 4 reference files       |
| 8    | COACTVWC   | 941   | Online  | 7          | 7    | 7               | **7.0**   | Account view; reads 3 VSAM files               |
| 9    | COCRDSLC   | 887   | Online  | 7          | 6    | 7               | **6.7**   | Card detail view; ABEND handling               |
| 10   | COTRN02C   | 783   | Online  | 7          | 7    | 7               | **7.0**   | Transaction add; writes to master file         |

---

## Detailed Hotspot Analysis

### #1 -- COACTUPC (Account Update) -- Composite: 9.7

**The single most complex and risky program in the entire codebase.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 4,236  | Extremely high (2.7x the next largest program) |
| IF Statements       | 168    | Very high branching complexity |
| EVALUATE Statements | 10     | High switch logic  |
| PERFORM Statements  | 64     | High call depth    |
| CICS Commands       | 17     | Heavy CICS usage   |
| Copybooks Included  | 56     | Extremely high (includes BMS + data copybooks) |
| Files Accessed      | 4      | ACCTDATA, CUSTDATA, CARDDATA, CARDXREF |

**Why It's #1:**
- **Complexity:** At 4,236 lines, this is by far the largest program. It handles both account AND customer data updates in a single monolithic program with 168 conditional branches. It includes extensive field-level validation (SSN, phone, zip, state code, FICO score, dates) with ~20 distinct validation subroutines.
- **Risk:** Directly modifies two critical master files (ACCTDATA and CUSTDATA) with CICS REWRITE. Contains PII fields (SSN, DOB, address). Any bug here can corrupt account/customer data. The 56 copybook includes create tight coupling to many data structures.
- **Business Impact:** Central to account management -- the primary function of the application. Used by all regular users for day-to-day operations.

**Modernization Recommendations:**
- Decompose into separate Account Update and Customer Update services
- Extract validation logic into a shared validation utility
- Implement proper transaction management (the CICS pseudo-conversational pattern must be converted to stateful sessions or REST+database transactions)
- Add field-level audit logging for PII changes

---

### #2 -- CBTRN02C (Transaction Posting) -- Composite: 9.3

**The heart of the batch cycle -- posts daily transactions to the master file.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 731    | High              |
| IF Statements       | 93     | Very high         |
| PERFORM Statements  | 61     | High              |
| Copybooks Included  | 5      | Moderate          |
| Files Accessed      | 7+     | Very high (DALYTRAN, TRANSACT, XREF, DISCGRP, TCATBALF, TRANTYPE, TRANCATG) |

**Why It's #2:**
- **Complexity:** Reads from 7+ files simultaneously. Complex business logic for transaction validation, category balance updates, and rejection handling. 93 IF statements indicate extensive conditional processing.
- **Risk:** This is the **highest financial risk** program. Incorrect posting corrupts the transaction master and category balances. Updates cascade to interest calculation (CBACT04C) and statement generation (CBSTM03A). Generates rejection records that must be audited.
- **Business Impact:** Runs nightly in the batch cycle. If it fails, the entire downstream batch chain (interest, statements, reports) cannot run. Transaction data is the lifeblood of the credit card system.

**Modernization Recommendations:**
- Convert to a Spring Batch job with chunk-oriented processing
- Implement database transactions with proper rollback
- Add comprehensive error handling and dead-letter queue for rejections
- Build reconciliation reports to verify posting accuracy
- This should be among the last programs converted due to its centrality

---

### #3 -- CBACT04C (Interest Calculation) -- Composite: 9.0

**Calculates interest charges on account balances by category.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 652    | High              |
| IF Statements       | 86     | Very high         |
| PERFORM Statements  | 56     | High              |
| Copybooks Included  | 5      | Moderate          |
| Files Accessed      | 4      | TRANSACT, DISCGRP, TCATBALF, ACCTDATA |

**Why It's #3:**
- **Complexity:** 86 IF statements drive complex interest rate lookup logic. Joins Disclosure Group rates with category balances across multiple VSAM files. Signed decimal arithmetic with PIC S9(10)V99 fields requires precise BigDecimal handling in Java.
- **Risk:** Financial calculation accuracy is paramount. Updates both ACCTDATA (balance adjustments) and TCATBALF (category resets). Errors directly impact customer billing and revenue recognition. Subject to regulatory scrutiny.
- **Business Impact:** Runs nightly after transaction posting. Directly affects customer bills and company revenue. Must produce auditable results that can be verified against source data.

**Modernization Recommendations:**
- Implement with high-precision arithmetic (BigDecimal, never float/double)
- Build parallel-run capability to compare COBOL vs Java results
- Add comprehensive audit trail for all interest calculations
- Externalize rate tables to a database (currently in VSAM DISCGRP)

---

### #4 -- COCRDLIC (Card List) -- Composite: 8.0

**Lists credit cards with forward/backward paging via CICS browse commands.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 1,459  | Very high         |
| IF Statements       | 122    | Very high         |
| EVALUATE Statements | 18     | Very high         |
| CICS Commands       | 18     | Very high         |
| CALL Statements     | 3      | Moderate          |
| Copybooks Included  | 13     | High              |

**Why It's #4:**
- **Complexity:** Second-largest online program. The CICS browse pattern (STARTBR/READNEXT/READPREV/ENDBR) for pageable lists is one of the most complex CICS patterns to convert to Java. 18 EVALUATE statements handle multiple screen states and navigation paths. XCTLs to both COCRDSLC (view) and COCRDUPC (update).
- **Risk:** Browse cursors must be properly managed (ENDBR on all paths). Selection dispatching to view/update programs must maintain state correctly through COMMAREA.
- **Business Impact:** Primary card management interface. Users rely on this for day-to-day card operations.

**Modernization Recommendations:**
- Convert pageable list to a paginated REST API with offset/limit
- Implement server-side cursor or keyset pagination instead of CICS browse
- Split the view/update dispatch into separate REST endpoints

---

### #5 -- COCRDUPC (Card Update) -- Composite: 8.0

**Accepts and processes credit card detail updates.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 1,560  | Very high         |
| IF Statements       | 148    | Extremely high    |
| EVALUATE Statements | 16     | Very high         |
| CICS Commands       | 12     | High              |
| Copybooks Included  | 15     | High              |

**Why It's #5:**
- **Complexity:** 148 IF statements -- the second-highest conditional count in the codebase. Extensive field validation similar to COACTUPC. 16 EVALUATE statements for screen state management. Multi-screen pseudo-conversational pattern.
- **Risk:** Modifies card data (CARDDATA VSAM) via CICS REWRITE. Card number and CVV are sensitive PCI-DSS data. Incorrect updates can disable cards or create security vulnerabilities.
- **Business Impact:** Essential for card lifecycle management (activation, deactivation, updates). PCI compliance requires strict controls.

**Modernization Recommendations:**
- Implement PCI-DSS compliant data handling (card number masking, CVV encryption)
- Extract validation into shared validators (reuse with COACTUPC patterns)
- Add change audit trail for all card modifications

---

### #6 -- CBSTM03A (Statement Generation) -- Composite: 7.7

**Generates account statements in both plain text and HTML formats.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 924    | High              |
| CALL Statements     | 14     | Very high (calls CBSTM03B extensively) |
| Files Accessed      | 6      | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE, STMTFILE, HTMLFILE |
| Uses ALTER          | Yes    | Legacy control flow pattern |

**Why It's #6:**
- **Complexity:** Uses the **ALTER** verb -- one of the most problematic COBOL constructs for modernization. ALTER dynamically changes GO TO targets at runtime, making control flow analysis extremely difficult. Calls CBSTM03B 14 times for delegated file I/O. Reads 4 VSAM files and writes 2 output files simultaneously.
- **Risk:** ALTER makes testing and verification very challenging. The subroutine call pattern to CBSTM03B is unusual (uses a flag-driven interface). Statement output format must be pixel-perfect for customer-facing documents.
- **Business Impact:** Produces customer-facing statements. Format accuracy is legally required. Runs at the end of the batch cycle.

**Modernization Recommendations:**
- **Top priority:** Eliminate ALTER verb by refactoring to structured control flow before conversion
- Replace CBSTM03B subroutine pattern with standard method calls
- Convert to a template-based report engine (e.g., JasperReports or Thymeleaf)
- Consider generating PDF directly instead of text + HTML

---

### #7 -- CBTRN03C (Transaction Report) -- Composite: 7.3

**Generates the Daily Transaction Report with type/category lookups.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 649    | High              |
| IF Statements       | 75     | High              |
| EVALUATE Statements | 4      | Moderate          |
| PERFORM Statements  | 72     | Very high         |
| Files Accessed      | 5      | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM |

**Why It's #7:**
- **Complexity:** 72 PERFORM statements indicate deep call nesting. Joins transaction data with 3 reference files (cross-ref, type, category) for enriched reporting. Report formatting logic with page breaks, subtotals, and grand totals.
- **Risk:** Report accuracy is essential for reconciliation and audit. Date parameter handling (DATEPARM input file) adds an external dependency.
- **Business Impact:** Used for daily operational monitoring and regulatory reporting. Feeds into management decision-making.

**Modernization Recommendations:**
- Convert to a Spring Batch report job
- Use a reporting framework for formatting (vs manual string building)
- Externalize date parameters to application configuration

---

### #8 -- COACTVWC (Account View) -- Composite: 7.0

**Displays account details with customer and card cross-reference data.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 941    | High              |
| IF Statements       | 57     | Moderate-high     |
| EVALUATE Statements | 10     | High              |
| CICS Commands       | 15     | High              |
| Files Accessed      | 3      | ACCTDATA, CUSTDATA, CARDXREF |

**Why It's #8:**
- **Complexity:** Reads from 3 VSAM files to assemble a composite view. Multi-step data retrieval pattern with error handling for each file access. ABEND handling with HANDLE ABEND.
- **Risk:** Read-only but accesses sensitive customer data (PII). ABEND handling pattern must be correctly converted. The HANDLE ABEND/ABEND pattern is CICS-specific.
- **Business Impact:** Most-used screen for customer service representatives viewing account information.

**Modernization Recommendations:**
- Convert to a read-only REST GET endpoint
- Implement as a DTO assembler pattern joining Account + Customer + Card data
- Add access control and audit logging for PII viewing

---

### #9 -- COCRDSLC (Card Detail View) -- Composite: 6.7

**Displays detailed credit card information.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 887    | High              |
| IF Statements       | 68     | Moderate-high     |
| EVALUATE Statements | 8      | Moderate          |
| CICS Commands       | 14     | High              |
| ABEND Handling      | Yes    | HANDLE ABEND + explicit ABEND |

**Why It's #9:**
- **Complexity:** Includes both HANDLE ABEND setup and explicit EXEC CICS ABEND calls -- an unusual dual error-handling pattern. Complex screen state management across pseudo-conversational iterations.
- **Risk:** Displays sensitive card data (card number, CVV). The explicit ABEND call is a drastic error-handling approach that terminates the CICS task -- needs careful conversion to exception handling.
- **Business Impact:** Key screen for card inquiry workflows. Used by customer service for card-related questions.

**Modernization Recommendations:**
- Replace ABEND pattern with proper exception handling (try/catch)
- Implement PCI-DSS compliant card data display (masking)
- Convert to REST GET endpoint with appropriate security controls

---

### #10 -- COTRN02C (Transaction Add) -- Composite: 7.0

**Allows online addition of new transactions to the TRANSACT master file.**

| Metric              | Value  | Assessment        |
|---------------------|--------|-------------------|
| Lines of Code       | 783    | High              |
| EVALUATE Statements | 13     | High              |
| PERFORM Statements  | 61     | High              |
| CICS Commands       | 11     | High              |
| CALL Statements     | 2      | Moderate          |
| Files Accessed      | 1+     | TRANSACT (write)  |

**Why It's #10:**
- **Complexity:** 13 EVALUATE statements manage a complex multi-state screen flow. Generates unique transaction IDs (timestamp-based). Validates transaction data before CICS WRITE to master file.
- **Risk:** Directly writes to the TRANSACT master file. Incorrect transactions affect downstream batch processing (posting, interest, statements). Transaction ID generation must be unique under concurrent access.
- **Business Impact:** Primary interface for manual transaction entry. Used for adjustments, corrections, and special transactions.

**Modernization Recommendations:**
- Convert to a POST REST endpoint with proper request validation
- Implement UUID-based transaction ID generation (replace timestamp-based)
- Add optimistic locking for concurrent access safety
- Implement input sanitization and business rule validation

---

## Risk Heat Map

Visual summary of where risk concentrates across the codebase.

```
                    Business Impact
                Low ◄──────────────────────► High
           ┌────────────────────────────────────────┐
     High  │                    │ COACTUPC  CBTRN02C │
           │                    │ COCRDUPC  CBACT04C │
           │                    │                    │
Complexity │ CBSTM03B          │ COCRDLIC  CBSTM03A │
           │                    │ COCRDSLC  CBTRN03C │
           │                    │ COTRN02C  COACTVWC │
           │                    │                    │
     Low   │ COBSWAIT  UNUSED1Y│ COSGN00C  COUSR01C │
           │ CSUTLDTC          │ COMEN01C  COUSR02C │
           │                    │ COADM01C  COUSR03C │
           └────────────────────────────────────────┘
```

**Red Zone (High Complexity + High Impact):** COACTUPC, CBTRN02C, CBACT04C, COCRDUPC
**Orange Zone (High Complexity OR High Impact):** COCRDLIC, CBSTM03A, CBTRN03C, COACTVWC, COCRDSLC, COTRN02C
**Green Zone (Low Complexity + Low Impact):** COBSWAIT, CSUTLDTC, CBSTM03B, menu/navigation programs

---

## Recommended Migration Waves

Based on the hotspot analysis, dependency clusters, and risk assessment:

### Wave 0: Foundation (Weeks 1-2)
**Goal:** Establish shared infrastructure before converting any business logic.

| Component       | Rationale                                                |
|-----------------|----------------------------------------------------------|
| CSUTLDTC        | Shared date utility called by 14 programs. Convert first. |
| COCOM01Y        | COMMAREA -> Java session/context object                   |
| CSUSR01Y        | Security record -> Spring Security UserDetails            |
| COTTL01Y + CSDAT01Y + CSMSG01Y | Shared UI components and messages        |
| All copybooks   | Convert to Java POJOs/entities (DATA_DICTIONARY.md)      |
| VSAM files      | Design and create relational database schema              |

### Wave 1: Low-Risk, High-Value (Weeks 3-5)
**Goal:** Convert simple, user-facing screens to build confidence.

| Component       | Complexity | Risk  | Rationale                                    |
|-----------------|------------|-------|----------------------------------------------|
| COSGN00C        | Low        | Low   | Simple authentication; validates approach     |
| COMEN01C        | Low        | Low   | Menu routing; validates navigation pattern    |
| COADM01C        | Low        | Low   | Admin menu; same pattern as COMEN01C          |
| COUSR00C-03C    | Low-Med    | Low   | Simple CRUD on USRSEC; validates VSAM->DB     |
| COTRN01C        | Low        | Low   | Read-only transaction view                    |

### Wave 2: Medium Complexity (Weeks 6-9)
**Goal:** Convert the read-heavy screens and basic batch jobs.

| Component       | Complexity | Risk  | Rationale                                    |
|-----------------|------------|-------|----------------------------------------------|
| COACTVWC        | Medium     | Medium| Read-only account view; validates multi-file read |
| COCRDSLC        | Medium     | Medium| Read-only card detail; ABEND handling         |
| COTRN00C        | Medium     | Medium| Transaction list; validates browse pattern    |
| COBIL00C        | Medium     | Medium| Bill payment; validates write pattern         |
| CBACT01C-03C    | Low        | Low   | Simple batch readers; validates batch pattern |
| CBCUS01C        | Low        | Low   | Simple batch reader                           |
| CBEXPORT/IMPORT | Medium     | Low   | Data migration utilities                      |

### Wave 3: High Complexity (Weeks 10-14)
**Goal:** Convert the complex business logic programs.

| Component       | Complexity | Risk   | Rationale                                    |
|-----------------|------------|--------|----------------------------------------------|
| COCRDLIC        | High       | Medium | Complex browse/paging; needs pagination design|
| COCRDUPC        | High       | High   | Card update; PCI implications                 |
| COTRN02C        | High       | High   | Transaction add; master file write            |
| CORPT00C        | Medium     | Medium | Report submission; TDQ pattern                |
| CBTRN03C        | High       | Medium | Report generation                             |

### Wave 4: Critical Business Logic (Weeks 15-20)
**Goal:** Convert the highest-risk programs with parallel-run validation.

| Component       | Complexity  | Risk   | Rationale                                    |
|-----------------|-------------|--------|----------------------------------------------|
| COACTUPC        | Very High   | Critical| Largest program; must decompose first        |
| CBTRN02C        | High        | Critical| Transaction posting; must parallel-run       |
| CBACT04C        | High        | Critical| Interest calculation; financial accuracy     |
| CBSTM03A/B      | High        | High   | Statement gen; ALTER elimination required     |
| COBSWAIT        | Trivial     | Low    | Replace with Thread.sleep or scheduler delay  |

### Wave 5: Optional Modules (Weeks 21+)
**Goal:** Convert the optional IMS/DB2/MQ modules if in scope.

| Component          | Rationale                                              |
|--------------------|--------------------------------------------------------|
| Authorization Module | IMS DB + MQ patterns; most complex integration       |
| Transaction Type DB2 | DB2 CRUD; validates embedded SQL conversion          |
| VSAM-MQ Module     | MQ request/response; validates messaging conversion   |

---

## Quick Wins

Programs that offer high modernization value with low conversion risk:

| Program    | LOC | Why It's a Quick Win                                              |
|------------|-----|-------------------------------------------------------------------|
| COBSWAIT   | 41  | Trivial utility; replace with `Thread.sleep()`                    |
| CSUTLDTC   | 157 | Simple date utility; replace with `java.time` API                 |
| COSGN00C   | 260 | Simple login; replace with Spring Security                        |
| COMEN01C   | 308 | Menu routing; replace with REST controller or SPA router          |
| COADM01C   | 288 | Admin menu; same pattern as COMEN01C                              |
| COUSR01C   | 299 | Simple user add; validates CICS WRITE -> JPA save                 |
| COTRN01C   | 330 | Read-only view; validates CICS READ -> JPA findById               |
| COUSR03C   | 359 | Simple delete; validates CICS DELETE -> JPA delete                 |
| CBACT02C   | 178 | Batch reader; validates sequential file -> Spring Batch ItemReader |
| CBACT03C   | 178 | Batch reader; same pattern as CBACT02C                            |
