# HOTSPOT REPORT - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Methodology:** Static analysis scoring across complexity, risk, and business impact
> **Purpose:** Prioritize modernization effort by identifying the top 10 highest-risk modules

---

## Table of Contents

1. [Scoring Methodology](#1-scoring-methodology)
2. [Top 10 Hotspot Ranking](#2-top-10-hotspot-ranking)
3. [Detailed Analysis per Module](#3-detailed-analysis-per-module)
4. [Modernization Priority Matrix](#4-modernization-priority-matrix)
5. [Risk Mitigation Recommendations](#5-risk-mitigation-recommendations)

---

## 1. Scoring Methodology

Each module is scored across three dimensions on a 1-10 scale:

| Dimension | Weight | Criteria |
|-----------|-------:|---------|
| **Complexity** | 40% | Lines of code, PERFORM count, EVALUATE/IF count, number of paragraphs, COPY includes, REDEFINES usage, file I/O operations |
| **Risk** | 35% | Number of VSAM files accessed, data update operations (WRITE/REWRITE/DELETE), COMMAREA dependencies, external CALLs, error handling paths |
| **Business Impact** | 25% | Criticality to business operations, financial data handling, number of downstream dependents, user-facing visibility, batch cycle position |

**Composite Score** = (Complexity x 0.40) + (Risk x 0.35) + (Business Impact x 0.25)

---

## 2. Top 10 Hotspot Ranking

| Rank | Program | Lines | Complexity | Risk | Business Impact | **Composite** | Domain |
|-----:|---------|------:|-----------:|-----:|----------------:|----------:|--------|
| **1** | **COACTUPC** | 4,236 | 10 | 9 | 9 | **9.40** | Account Update |
| **2** | **CBTRN02C** | 731 | 8 | 10 | 10 | **9.20** | Transaction Posting |
| **3** | **CBACT04C** | 652 | 8 | 9 | 10 | **8.85** | Interest Calculation |
| **4** | **COCRDLIC** | 1,459 | 9 | 7 | 7 | **7.80** | Card List |
| **5** | **COCRDUPC** | 1,560 | 9 | 8 | 6 | **7.90** | Card Update |
| **6** | **CBSTM03A** | 924 | 7 | 7 | 9 | **7.50** | Statement Generation |
| **7** | **CBTRN03C** | 649 | 7 | 6 | 8 | **6.90** | Transaction Report |
| **8** | **COBIL00C** | 572 | 6 | 8 | 9 | **7.45** | Bill Payment |
| **9** | **COTRN02C** | 783 | 7 | 7 | 7 | **7.00** | Transaction Add |
| **10** | **COACTVWC** | 941 | 7 | 6 | 6 | **6.40** | Account View |

---

## 3. Detailed Analysis per Module

### Rank 1: COACTUPC -- Account Update (Composite: 9.40)

**File:** `app/cbl/COACTUPC.cbl` | **Lines:** 4,236 | **Type:** Online CICS | **Transaction:** CAUP

#### Why It's #1
- **Largest program in the codebase** at 4,236 lines -- more than double the next largest
- Highest EVALUATE/IF count: 125 conditional branches (10 EVALUATE + 115 IF)
- 64 PERFORM statements, 120 paragraph labels
- Accesses 3 VSAM files with both READ and REWRITE operations
- Extensive input validation: SSN, phone numbers, dates (birth, open, expiry, reissue), credit limits, FICO scores
- REDEFINES used extensively for numeric/alphanumeric field conversions

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 4,236 |
| PERFORM Statements | 64 |
| EVALUATE Statements | 10 |
| IF Statements | 115 |
| Paragraph Labels | 120 |
| VSAM Files Accessed | 3 (ACCTDAT R/W, CUSTDAT R/W, CARDDAT R) |
| Copybooks Included | 12+ |
| External Calls | 0 (self-contained) |

#### Modernization Risks
- **Massive validation logic** must be carefully preserved -- SSN format rules, phone parsing, date leap-year checks
- **REDEFINES chains** for numeric editing require careful Java type mapping
- **BMS map interaction** (COACTUP) tightly coupled to screen layout
- Multiple **COMMAREA state transitions** (fetch -> edit -> validate -> confirm -> commit)
- **Concurrent update handling** via READ-FOR-UPDATE / REWRITE pattern

#### Recommended Approach
- Decompose into separate service classes: AccountValidationService, AccountPersistenceService, AccountScreenController
- Extract validation rules into a reusable validation framework
- Map VSAM READ/REWRITE to JPA repository pattern with optimistic locking

---

### Rank 2: CBTRN02C -- Transaction Posting (Composite: 9.20)

**File:** `app/cbl/CBTRN02C.cbl` | **Lines:** 731 | **Type:** Batch | **JCL:** POSTTRAN

#### Why It's #2
- **Core business logic engine** -- posts daily transactions to the master file
- Touches the most VSAM files of any single program (7 files)
- Critical position in the batch cycle: all downstream jobs depend on its output
- Complex business rules: expiration validation, category balance updates, rejection handling

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 731 |
| PERFORM Statements | 61 |
| IF Statements | 41 |
| VSAM Files Read | DALYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBALF, DISCGRP, TRANTYPE |
| VSAM Files Written | TRANSACT, ACCTDATA, TCATBALF |
| Rejection Output | DALYREJS |
| External Calls | CEE3ABD (abend handler) |

#### Modernization Risks
- **Multi-file transaction consistency** -- updates span 3+ files without ACID guarantees (VSAM has no commit/rollback)
- **Rejection handling** must be preserved precisely -- regulatory reporting may depend on it
- **Category balance accumulation** logic is foundational to interest calculation
- **Batch-to-online timing** -- must not conflict with online CICS file access

#### Recommended Approach
- Convert to Spring Batch job with chunk-oriented processing
- Wrap multi-table updates in database transactions (ACID)
- Implement rejection records as a separate error handling service
- Add comprehensive audit logging (not present in COBOL)

---

### Rank 3: CBACT04C -- Interest Calculation (Composite: 8.85)

**File:** `app/cbl/CBACT04C.cbl` | **Lines:** 652 | **Type:** Batch | **JCL:** INTCALC

#### Why It's #3
- **Financial calculation engine** -- directly impacts customer balances
- Reads from 3 VSAM files, writes to 1 (account balances)
- 36 IF statements + 56 PERFORM statements indicate significant branching logic
- Date-driven processing (PARM='YYYYMMDD00' parameter)
- Lookup-driven interest rates from disclosure group table

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 652 |
| PERFORM Statements | 56 |
| IF Statements | 36 |
| VSAM Files Read | ACCTDATA, TCATBALF, DISCGRP |
| VSAM Files Written | ACCTDATA (balance update) |
| JCL Parameter | Date parameter (PARM='YYYYMMDD00') |

#### Modernization Risks
- **Financial precision** -- COBOL `S9(10)V99` arithmetic has exact decimal semantics; Java `BigDecimal` required (never `double`)
- **Interest rate lookups** involve multi-key composite reads (account group + tran type + category)
- **Regulatory compliance** -- interest calculation rules may be subject to audit
- **Date handling** via JCL PARM must be converted to configuration/scheduling

#### Recommended Approach
- Convert to Spring Batch with `BigDecimal` arithmetic exclusively
- Externalize interest rate configuration (database or config file)
- Add calculation audit trail for regulatory compliance
- Implement extensive unit tests comparing COBOL and Java calculation outputs

---

### Rank 4: COCRDLIC -- Credit Card List (Composite: 7.80)

**File:** `app/cbl/COCRDLIC.cbl` | **Lines:** 1,459 | **Type:** Online CICS | **Transaction:** CCLI

#### Why It's #4
- Second largest online program by line count
- Complex **browse/pagination** logic using CICS STARTBR/READNEXT/READPREV/ENDBR
- 53 conditional branches (18 EVALUATE + 35 IF)
- Multi-action processing: list, view detail ('S'), update ('U') from same screen
- XCTL to 3 different programs (menu, card detail, card update)

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 1,459 |
| PERFORM Statements | 34 |
| EVALUATE + IF | 53 |
| Browse Operations | STARTBR, READNEXT x2, READPREV x2, ENDBR x2 |
| XCTL Targets | COMEN01C, COCRDSLC, COCRDUPC |
| VSAM Files | CARDDAT (browse), CARDAIX (alternate index) |

#### Modernization Risks
- **Pagination state management** via COMMAREA requires stateless conversion (offset/cursor-based pagination in REST)
- **Dual index browsing** (primary key and alternate index) must map to database queries
- **Screen-driven flow** (select row -> navigate) must become API + UI interaction pattern

---

### Rank 5: COCRDUPC -- Credit Card Update (Composite: 7.90)

**File:** `app/cbl/COCRDUPC.cbl` | **Lines:** 1,560 | **Type:** Online CICS | **Transaction:** CCUP

#### Why It's #5
- Third largest program by line count
- Complex update workflow: fetch -> display -> edit -> validate -> confirm -> commit
- 63 conditional branches (16 EVALUATE + 47 IF)
- Multi-state COMMAREA tracking (CCUP-CHANGE-ACTION with 8 possible states)
- Optimistic locking pattern: read original, compare before rewrite

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 1,560 |
| PERFORM Statements | 26 |
| EVALUATE + IF | 63 |
| VSAM Files | CARDDAT (R/W), CUSTDAT (R) |
| State Machine States | 8 (not-fetched, show, edit, not-ok, confirmed, done, lock-error, failed) |

#### Modernization Risks
- **State machine logic** embedded in COMMAREA must be converted to proper state pattern or form wizard
- **Concurrent update detection** relies on comparing all field values before/after
- **Alphabetic-only name validation** uses INSPECT TALLYING pattern

---

### Rank 6: CBSTM03A -- Statement Generation (Composite: 7.50)

**File:** `app/cbl/CBSTM03A.CBL` | **Lines:** 924 | **Type:** Batch | **JCL:** CREASTMT

#### Why It's #6
- Generates customer-facing financial statements (text + HTML)
- Calls CBSTM03B as a subroutine 11 times for file I/O
- Reads from 4 VSAM files (via CBSTM03B helper)
- Produces dual-format output (plain text and HTML)
- Uses ALTER verb (GO TO ... DEPENDING ON pattern) -- rare and hard to convert

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 924 (+ 230 in CBSTM03B) |
| CALL to CBSTM03B | 11 calls |
| Input Files | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE |
| Output Files | STMTFILE (text), HTMLFILE (HTML) |
| Uses ALTER verb | Yes (dynamic GO TO modification) |

#### Modernization Risks
- **ALTER verb** is one of the hardest COBOL constructs to convert -- modifies GO TO targets at runtime
- **HTML generation** is embedded inline with COBOL string manipulation
- **Multi-file correlation** (card -> xref -> customer -> account -> transactions) requires complex join logic
- **Report formatting** with exact column alignment must be preserved

#### Recommended Approach
- Replace ALTER with proper control flow (switch/state pattern)
- Use a template engine (Thymeleaf/FreeMarker) for HTML statement generation
- Convert to Spring Batch with ItemReader/ItemProcessor/ItemWriter pattern

---

### Rank 7: CBTRN03C -- Transaction Report (Composite: 6.90)

**File:** `app/cbl/CBTRN03C.cbl` | **Lines:** 649 | **Type:** Batch | **JCL:** TRANREPT

#### Why It's #7
- Complex report generation with multi-level breaks (account, page, grand total)
- 72 PERFORM statements (highest in the codebase)
- Reads from multiple reference files (TRANTYPE, TRANCATG) for description lookups
- Formatted output with control break logic

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 649 |
| PERFORM Statements | 72 |
| EVALUATE + IF | 35 |
| Report Levels | Page, Account, Grand Total |

---

### Rank 8: COBIL00C -- Bill Payment (Composite: 7.45)

**File:** `app/cbl/COBIL00C.cbl` | **Lines:** 572 | **Type:** Online CICS | **Transaction:** CB00

#### Why It's #8
- **Financial transaction creation** -- directly modifies account balances and creates payment transactions
- Accesses both ACCTDAT and TRANSACT with READ + REWRITE + WRITE
- Browse operations for finding last transaction ID (for sequence generation)
- Business-critical: incorrect bill payment logic = financial loss

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 572 |
| PERFORM Statements | 38 |
| EVALUATE + IF | 14 |
| VSAM Operations | READ, REWRITE (ACCTDAT), WRITE (TRANSACT), STARTBR, READPREV, ENDBR |

#### Modernization Risks
- **Transaction ID generation** via browse to last record + increment is fragile
- **Balance zeroing** must be atomic with payment transaction creation
- No explicit error recovery if WRITE succeeds but REWRITE fails

---

### Rank 9: COTRN02C -- Transaction Add (Composite: 7.00)

**File:** `app/cbl/COTRN02C.cbl` | **Lines:** 783 | **Type:** Online CICS | **Transaction:** CT02

#### Why It's #9
- User-facing transaction creation screen
- Calls CSUTLDTC for date validation (2 calls)
- Accesses TRANSACT (R/W) and ACCTDAT (R) and CARDXREF (via browse)
- Complex date validation logic with start/end date comparison

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 783 |
| PERFORM Statements | 61 |
| EVALUATE + IF | 20 |
| External Calls | CSUTLDTC x 2 |
| VSAM Operations | READ, WRITE, STARTBR, READPREV, ENDBR |

---

### Rank 10: COACTVWC -- Account View (Composite: 6.40)

**File:** `app/cbl/COACTVWC.cbl` | **Lines:** 941 | **Type:** Online CICS | **Transaction:** CAVW

#### Why It's #10
- Read-only account display but complex data assembly
- Reads from 3 VSAM files (ACCTDAT, CARDDAT via AIX, CUSTDAT)
- XCTL navigation to account update program
- String processing via CSSTRPFY copybook include

#### Metrics
| Metric | Value |
|--------|------:|
| Lines of Code | 941 |
| PERFORM Statements | 21 |
| EVALUATE + IF | 27 |
| VSAM Reads | ACCTDAT, CARDDAT (via DATASET), CUSTDAT (via DATASET) |
| XCTL Target | COACTUPC (dynamic via CDEMO-TO-PROGRAM) |

---

## 4. Modernization Priority Matrix

Based on the hotspot analysis, recommended modernization order:

### Phase 1: Core Financial Engine (Highest Risk)
| Priority | Module | Rationale |
|----------|--------|-----------|
| P1.1 | **CBTRN02C** | Transaction posting is the heartbeat of the system. Convert first to establish batch framework patterns. |
| P1.2 | **CBACT04C** | Interest calculation directly depends on CBTRN02C output. Financial precision is paramount. |
| P1.3 | **COBIL00C** | Bill payment creates financial transactions online. Must align with new transaction model. |

### Phase 2: Account & Card Management (High Complexity)
| Priority | Module | Rationale |
|----------|--------|-----------|
| P2.1 | **COACTUPC** | Largest program. Decompose into microservices. Establish validation patterns for reuse. |
| P2.2 | **COCRDUPC** | Card update follows similar patterns to account update. Reuse validation framework. |
| P2.3 | **COCRDLIC** | Card list establishes browse/pagination patterns reusable across all list screens. |

### Phase 3: Reporting & Statements (Medium Risk)
| Priority | Module | Rationale |
|----------|--------|-----------|
| P3.1 | **CBSTM03A** | Statement generation. ALTER verb makes this tricky. Convert after batch framework is solid. |
| P3.2 | **CBTRN03C** | Report generation follows established batch patterns from Phase 1. |

### Phase 4: Remaining Online Programs (Lower Risk)
| Priority | Module | Rationale |
|----------|--------|-----------|
| P4.1 | **COTRN02C** | Transaction add. Follows patterns established in earlier phases. |
| P4.2 | **COACTVWC** | Read-only view. Lower risk, can leverage Phase 2 data access layer. |
| P4.3 | Remaining programs | COSGN00C, COMEN01C, COADM01C, COTRN00C, COTRN01C, COUSR00C-03C |

---

## 5. Risk Mitigation Recommendations

### Technical Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Financial precision loss** | Critical | Use `BigDecimal` exclusively. Never use `float`/`double` for money. Write comparison tests against COBOL output. |
| **VSAM-to-RDBMS data integrity** | High | Implement database transactions (ACID) where COBOL had none. Add optimistic locking (version columns). |
| **ALTER verb conversion (CBSTM03A)** | High | Manually rewrite control flow. Use state pattern or strategy pattern. Extensive testing required. |
| **COMMAREA state machine** | Medium | Convert to proper state machine pattern (Spring Statemachine) or session-scoped beans. |
| **REDEFINES/INSPECT patterns** | Medium | Map REDEFINES to Java type conversion methods. INSPECT to regex or String utility methods. |
| **Concurrent update handling** | Medium | Replace COBOL "compare all fields" pattern with JPA `@Version` optimistic locking. |
| **Packed decimal (COMP-3)** | Low | Use `BigDecimal`. Ensure ETL data migration handles BCD-to-decimal conversion correctly. |

### Testing Strategy

| Phase | Testing Approach |
|-------|-----------------|
| **Data Migration** | Byte-for-byte comparison of VSAM exports vs. database loads. Validate all 11 VSAM files. |
| **Financial Calculations** | Run COBOL and Java interest calculations on identical test data. Compare to the penny. |
| **Transaction Posting** | Process identical daily transaction files through both systems. Compare all output files. |
| **Statement Generation** | Generate statements from both systems. Diff text and HTML output character by character. |
| **Online Screens** | Map every 3270 screen field to web UI field. Automated UI testing for all CRUD flows. |
| **Batch Cycle** | Run full nightly batch cycle in both systems. Compare all VSAM/database states after completion. |

### Key Metrics to Track During Modernization

| Metric | Target |
|--------|--------|
| Programs converted | 31 core + 13 optional = 44 |
| Test coverage (line) | >90% for top 10 hotspots |
| Financial calculation accuracy | 100% (zero tolerance) |
| Batch cycle completion time | Equal or faster than COBOL |
| User acceptance test pass rate | >95% |
| Data migration validation | 100% record match |
