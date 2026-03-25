# CardDemo Risk Register

## 1. Overview

This register identifies the top risks associated with modernizing the CardDemo mainframe application from COBOL/CICS/VSAM to Java/Spring Boot/PostgreSQL. Each risk is assessed for likelihood, impact, and overall severity, with concrete mitigations grounded in the actual codebase analysis.

**Risk Scoring**:
- **Likelihood**: Low (1), Medium (2), High (3)
- **Impact**: Low (1), Medium (2), High (3)
- **Severity** = Likelihood x Impact (1-3 = Low, 4-6 = Medium, 7-9 = High)

---

## 2. Risk Summary

| ID | Risk | Likelihood | Impact | Severity | Category |
|----|------|:----------:|:------:|:--------:|----------|
| R-01 | Financial calculation precision loss | Medium | High | **6 - Medium** | Technical |
| R-02 | VSAM-to-RDBMS data migration integrity | Medium | High | **6 - Medium** | Technical |
| R-03 | Transaction ID collision during dual-write | High | Medium | **6 - Medium** | Technical |
| R-04 | COMMAREA state management translation | Medium | Medium | **4 - Medium** | Technical |
| R-05 | Batch processing window constraints | Medium | High | **6 - Medium** | Operational |
| R-06 | Statement generation fidelity (ALTER/GO TO) | High | Medium | **6 - Medium** | Technical |
| R-07 | Concurrent account balance updates | High | High | **9 - High** | Technical |
| R-08 | Loss of tribal knowledge | Medium | High | **6 - Medium** | Organizational |
| R-09 | BMS screen-to-SPA functional parity | Medium | Medium | **4 - Medium** | Technical |
| R-10 | COBOL numeric precision (COMP-3/packed decimal) | Medium | High | **6 - Medium** | Technical |
| R-11 | JCL/TDQ pattern replacement | Low | Medium | **2 - Low** | Technical |
| R-12 | Regression in validation rules (COACTUPC) | High | High | **9 - High** | Technical |
| R-13 | Dual-write consistency failures | Medium | High | **6 - Medium** | Technical |
| R-14 | Performance degradation in migrated services | Medium | Medium | **4 - Medium** | Technical |
| R-15 | Mainframe decommission timing | Low | High | **3 - Low** | Operational |

---

## 3. Detailed Risk Analysis

### R-01: Financial Calculation Precision Loss
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: CBACT04C (interest calculation) uses COBOL COMP-3 (packed decimal) and signed numeric fields (PIC S9(09)V99) for interest computation. Java's `double` and `float` types cannot represent decimal fractions exactly, potentially causing rounding differences that accumulate across thousands of accounts.

**Evidence from codebase**:
- `CBACT04C.cbl` line 168: `WS-MONTHLY-INT PIC S9(09)V99` — 9 digits + 2 decimal places
- `CBACT04C.cbl` line 169: `WS-TOTAL-INT PIC S9(09)V99` — accumulated interest
- `CBACT04C.cbl` line 352: `ADD WS-TOTAL-INT TO ACCT-CURR-BAL` — balance update
- `CVACT01Y.cpy` line 7: `ACCT-CURR-BAL PIC S9(10)V99` — 10 digits + 2 decimal, signed

**Impact**: Financial discrepancies, regulatory non-compliance, customer disputes.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use `java.math.BigDecimal` for ALL financial calculations — never `double` or `float` | Backend | Required |
| 2 | Set rounding mode to `HALF_EVEN` (banker's rounding) to match COBOL default | Backend | Required |
| 3 | Build equivalence test harness: process identical TCATBALF input through both COBOL and Java, compare interest to the penny | QA | Required |
| 4 | Run parallel interest calculation for 3 consecutive months before cutover | QA | Required |

---

### R-02: VSAM-to-RDBMS Data Migration Integrity
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: Nine VSAM datasets with fixed-length records must be migrated to PostgreSQL tables. COBOL's implicit decimal positioning (PIC S9(10)V99 stores as 12-digit integer), EBCDIC-to-ASCII encoding, and packed decimal formats can cause data corruption if not converted correctly.

**Evidence from codebase**:
- `CVACT01Y.cpy`: ACCOUNT-RECORD is 300 bytes with 178 bytes of FILLER
- `CVCUS01Y.cpy`: CUSTOMER-RECORD is 500 bytes with 168 bytes of FILLER
- `CVTRA05Y.cpy`: TRAN-RECORD is 350 bytes with 20 bytes of FILLER
- ASCII data files exist in `app/data/ASCII/` but production would use EBCDIC

**Impact**: Corrupted account balances, incorrect customer data, orphaned records.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use the existing ASCII data files (`app/data/ASCII/`) for initial development and testing | Backend | Required |
| 2 | Build COBOL-to-Java field type mapping document from all 30 copybooks | Backend | Required |
| 3 | Validate record counts: source VSAM count = target table count for each entity | QA | Required |
| 4 | Validate checksums: sum of ACCT-CURR-BAL across all accounts must match between VSAM and database | QA | Required |
| 5 | Sample verification: manually verify 100 randomly selected records per entity | QA | Required |
| 6 | Handle FILLER fields explicitly — verify they are genuinely unused before dropping | Backend | Required |

---

### R-03: Transaction ID Collision During Dual-Write
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: Transaction IDs are generated via MAX(TRAN-ID) + 1 across multiple programs (COTRN02C, COBIL00C, CBTRN02C, CBACT04C). During dual-write, both legacy and modern systems may attempt to generate IDs simultaneously, causing collisions or gaps.

**Evidence from codebase**:
- `COBIL00C.cbl` lines 212-219: STARTBR with HIGH-VALUES, READPREV to get max ID, ADD 1
- `COTRN02C.cbl`: Same MAX+1 pattern for online transaction creation
- `CBTRN02C.cbl`: Batch posting writes to same TRANSACT file

**Impact**: Duplicate transaction IDs, lost transactions, data integrity violations.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use database SEQUENCE for ID generation in the modern system (guaranteed unique, no contention) | Backend | Required |
| 2 | During dual-write phase, partition ID ranges: legacy uses odd numbers, modern uses even numbers (or reserve a range) | Backend | Required |
| 3 | Add unique constraint on transaction ID in both VSAM and database | Backend | Required |
| 4 | Monitor for ID collision alerts during parallel run | DevOps | Required |

---

### R-04: COMMAREA State Management Translation
**Severity: 4 (Medium)** | **Category: Technical**

**Description**: The CARDDEMO-COMMAREA (COCOM01Y) carries session state across program transfers (XCTL). It includes user identity, selected customer/account/card, navigation history, and program re-entry state. Modern REST services are stateless, and this implicit state passing must be explicitly redesigned.

**Evidence from codebase**:
- `COCOM01Y.cpy`: 48 lines defining the shared communication area
- Every online program checks `CDEMO-PGM-REENTER` (88-level: 0=enter, 1=reenter) to determine first-time vs. re-entry behavior
- Navigation uses `CDEMO-FROM-PROGRAM` / `CDEMO-TO-PROGRAM` for return routing
- Context data: `CDEMO-ACCT-ID`, `CDEMO-CARD-NUM`, `CDEMO-CUST-ID`

**Impact**: Lost session context, broken navigation flows, incorrect data display.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Map COMMAREA fields to JWT claims (user ID, type) and API request parameters (account ID, card number) | Backend | Required |
| 2 | Eliminate re-entry flag — REST APIs are inherently stateless; each request is self-contained | Backend | Required |
| 3 | Handle navigation entirely in the SPA router (React Router) | Frontend | Required |
| 4 | Document every COMMAREA field's purpose and its modern equivalent | Backend | Recommended |

---

### R-05: Batch Processing Window Constraints
**Severity: 6 (Medium)** | **Category: Operational**

**Description**: Mainframe batch cycles (daily transaction posting via CBTRN02C, monthly interest via CBACT04C, statement generation via CBSTM03A) run within defined processing windows. During migration, both legacy and modern batch jobs may need to run, potentially exceeding the available window.

**Evidence from codebase**:
- 38 JCL files in `app/jcl/` define the batch job scheduling and dependencies
- CBTRN02C processes daily transaction files (DALYTRAN) — must complete before end-of-day
- CBACT04C runs monthly — must complete before statement generation

**Impact**: Delayed batch processing, missed SLAs, financial reporting errors.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Run modern batch jobs in a separate time window (not competing with legacy) during parallel period | DevOps | Required |
| 2 | Design Spring Batch jobs for restartability — failed jobs can resume from last checkpoint | Backend | Required |
| 3 | Use chunk-oriented processing with configurable commit intervals for throughput tuning | Backend | Required |
| 4 | Schedule cutover at the start of a new billing cycle, not mid-cycle | PM | Required |

---

### R-06: Statement Generation Fidelity (ALTER/GO TO Patterns)
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: CBSTM03A (924 LOC) uses several patterns that have no direct Java equivalent: ALTER statement (dynamic GO TO modification), mainframe control block addressing (PSA/TCB/TIOT via POINTER), COMP/COMP-3 variables, and 2D arrays with subroutine calls (CBSTM03B). The ALTER pattern modifies program flow at runtime — a self-modifying code pattern.

**Evidence from codebase**:
- `CBSTM03A.CBL` lines 300-311: `ALTER 8100-FILE-OPEN TO PROCEED TO 8100-TRNXFILE-OPEN` — dynamic dispatch
- `CBSTM03A.CBL` lines 266-270: PSA → TCB → TIOT pointer chain for JCL job/step name retrieval
- `CBSTM03A.CBL` lines 226-233: 2D array `WS-CARD-TBL OCCURS 51 TIMES` with nested `WS-TRAN-TBL OCCURS 10 TIMES`
- Generates both plain text and HTML output formats

**Impact**: Incorrect statement format, missing transactions on statements, incorrect totals.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Replace ALTER/GO TO dispatch with a strategy pattern or method-reference map in Java | Backend | Required |
| 2 | Replace PSA/TCB/TIOT addressing with standard Java system property access (job metadata) | Backend | Required |
| 3 | Use template engine (Thymeleaf/FreeMarker) for statement formatting — separates data from presentation | Backend | Required |
| 4 | Generate sample statements from both systems for the same accounts; pixel-diff compare | QA | Required |
| 5 | Preserve the 51-card x 10-transaction data structure as a Java collection with equivalent iteration logic | Backend | Recommended |

---

### R-07: Concurrent Account Balance Updates
**Severity: 9 (High)** | **Category: Technical**

**Description**: ACCT-CURR-BAL in the ACCOUNT-RECORD is modified by four separate concerns: bill payment (COBIL00C), batch transaction posting (CBTRN02C), interest calculation (CBACT04C), and account update (COACTUPC). In the legacy system, CICS provides record-level locking (READ UPDATE / REWRITE) and batch programs open files in I-O mode. In the modern system, concurrent REST API calls and batch jobs could corrupt the balance without proper concurrency control.

**Evidence from codebase**:
- `COBIL00C.cbl` line 234: `COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT`
- `CBTRN02C.cbl`: Updates ACCT-CURR-BAL during daily posting
- `CBACT04C.cbl` line 352: `ADD WS-TOTAL-INT TO ACCT-CURR-BAL`
- `COACTUPC.cbl`: Allows manual balance/credit-limit edits
- All four use READ UPDATE + REWRITE pattern for VSAM record locking

**Impact**: Lost updates, incorrect account balances, financial data corruption.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use JPA `@Version` for optimistic locking on the Account entity — prevent lost updates | Backend | Required |
| 2 | Use database-level `SELECT FOR UPDATE` for balance modifications — serialize concurrent writes | Backend | Required |
| 3 | Implement balance modification as an atomic operation: `UPDATE accounts SET balance = balance + ? WHERE id = ?` (not read-modify-write) | Backend | Required |
| 4 | Ensure batch jobs acquire row-level locks during balance updates | Backend | Required |
| 5 | Add balance audit trail table to track every modification with source, amount, and timestamp | Backend | Recommended |
| 6 | Implement balance reconciliation job that verifies sum(transactions) = current balance | QA | Recommended |

---

### R-08: Loss of Tribal Knowledge
**Severity: 6 (Medium)** | **Category: Organizational**

**Description**: The CardDemo COBOL programs contain embedded business rules that are not documented outside the code. For example, COACTUPC's 4,237 lines contain dozens of validation rules for SSN format, phone numbers, date ranges, credit limits, and status codes. If the developers who understand these rules are not available during modernization, rules may be missed or incorrectly reimplemented.

**Evidence from codebase**:
- `COACTUPC.cbl`: Inline validation rules spanning thousands of lines with no external specification
- `COBIL00C.cbl`: Business rule that bill payment amount equals full current balance (not partial)
- `CBTRN02C.cbl` line 377: Comment `* ADD MORE VALIDATIONS HERE` — implies incomplete validation
- `CBACT04C.cbl`: Interest calculation break logic embedded in control flow

**Impact**: Missing business rules in the target system, regulatory non-compliance, customer-facing errors.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Extract business rules from COBOL into a structured rules catalog before rewriting | BA/Architect | Required |
| 2 | Pair COBOL-experienced developers with Java developers during migration | PM | Required |
| 3 | Use automated COBOL analysis tools to generate data flow and control flow diagrams | Tooling | Recommended |
| 4 | Create comprehensive test cases from legacy system behavior (characterization tests) | QA | Required |
| 5 | Record legacy system walkthroughs with subject matter experts before decommission | PM | Recommended |

---

### R-09: BMS Screen-to-SPA Functional Parity
**Severity: 4 (Medium)** | **Category: Technical**

**Description**: The 17 BMS maps define 3270 terminal screens with specific field positions, attribute bytes (protected/unprotected, bright/normal), and cursor positioning. The React SPA must provide equivalent functionality without the rigid row/column layout. Users accustomed to keyboard-driven 3270 navigation (PF keys, tab order) may find the new UI unfamiliar.

**Evidence from codebase**:
- 17 BMS files in `app/bms/` defining screen layouts
- PF key handling in every online program: PF3 (back), PF7 (page up), PF8 (page down)
- Cursor positioning via `MOVE -1 TO fieldL` pattern for error field focus
- Protected fields for display-only data; unprotected for input

**Impact**: User confusion, reduced productivity, resistance to adoption.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Map PF key functions to keyboard shortcuts (Ctrl+← for back, PageUp/PageDown for pagination) | Frontend | Recommended |
| 2 | Maintain field tab order consistent with 3270 screen flow | Frontend | Recommended |
| 3 | Implement auto-focus on error fields (replicating cursor positioning behavior) | Frontend | Required |
| 4 | Conduct user acceptance testing with actual mainframe users before cutover | QA/PM | Required |
| 5 | Provide brief training materials showing 3270-to-web UI mapping | PM | Recommended |

---

### R-10: COBOL Numeric Precision (COMP-3/Packed Decimal)
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: COBOL uses several binary numeric formats: COMP (binary), COMP-3 (packed decimal), and display numeric (zoned decimal). Each has specific precision and storage characteristics. Java must handle these correctly during data migration and runtime computation.

**Evidence from codebase**:
- `CBSTM03A.CBL` line 64: `COMP3-VARIABLES COMP-3` with `PIC S9(9)V99`
- `CBSTM03A.CBL` line 59: `COMP-VARIABLES COMP` with `PIC S9(4)`
- `CVACT01Y.cpy`: `ACCT-CURR-BAL PIC S9(10)V99` — 12 storage digits, 2 implied decimal
- `CVTRA05Y.cpy`: `TRAN-AMT PIC S9(09)V99` — signed, 2 implied decimal

**Impact**: Data corruption during migration, incorrect calculations at runtime.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Map all COBOL PIC clauses to Java types: `PIC S9(n)V99` → `BigDecimal(n+2, 2)`, `COMP` → `int`/`long`, `COMP-3` → `BigDecimal` | Backend | Required |
| 2 | Build a COBOL field type catalog from all 30 copybooks | Backend | Required |
| 3 | Use `BigDecimal` for ALL monetary and decimal fields without exception | Backend | Required |
| 4 | Verify boundary values: max PIC S9(10)V99 = +9,999,999,999.99 fits in `BigDecimal` | Backend | Required |

---

### R-11: JCL/TDQ Pattern Replacement
**Severity: 2 (Low)** | **Category: Technical**

**Description**: CORPT00C submits JCL batch jobs via CICS extra-partition TDQ (Transient Data Queue) to the Internal Reader. This CICS-to-JES2 submission pattern has no direct modern equivalent.

**Evidence from codebase**:
- `CORPT00C.cbl` lines 81-127: JOB-DATA contains embedded JCL with 16 lines of job control
- JCL includes: `//TRNRPT00 JOB`, `//STEP10 EXEC PROC=TRANREPT`, `SYMNAMES DD *`, `DATEPARM DD *`
- 38 JCL files in `app/jcl/` defining various batch procedures

**Impact**: Report generation unavailable until replacement is built.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Replace TDQ job submission with REST API call to trigger Spring Batch job | Backend | Required |
| 2 | Map JCL parameters (date ranges, report types) to Spring Batch job parameters | Backend | Required |
| 3 | Use Spring Batch Admin or Spring Cloud Data Flow for job monitoring (replaces JES2 spool) | DevOps | Recommended |

---

### R-12: Regression in Validation Rules (COACTUPC)
**Severity: 9 (High)** | **Category: Technical**

**Description**: COACTUPC.cbl (4,237 LOC) is the largest program and contains the most complex validation logic in the system. It validates SSN format, phone numbers, date ranges, credit limits, account status transitions, and dozens of other business rules. Missing even one validation rule could allow invalid data into the system.

**Evidence from codebase**:
- 4,237 lines of COBOL with inline validation for every editable field
- Multiple validation patterns: numeric checks, range checks, format checks, cross-field checks
- Account status transition rules (active → inactive, etc.)
- Credit limit constraints relative to cash credit limit

**Impact**: Invalid data in production, regulatory violations, customer-facing errors.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Extract every validation rule into a testable rules catalog with input/expected-output pairs | BA | Required |
| 2 | Implement validations as a shared library with individual unit tests per rule | Backend | Required |
| 3 | Build a characterization test suite: feed 1000+ edge-case inputs to legacy, capture outputs, replay against modern | QA | Required |
| 4 | Use property-based testing (e.g., jqwik) to generate random inputs and verify both systems agree | QA | Recommended |
| 5 | Code review each validation rule with a COBOL SME before marking complete | Architect | Required |

---

### R-13: Dual-Write Consistency Failures
**Severity: 6 (Medium)** | **Category: Technical**

**Description**: During the transition period, writes must go to both VSAM and PostgreSQL. If one write succeeds and the other fails, the systems diverge. VSAM does not support distributed transactions with RDBMS.

**Evidence from codebase**:
- All CICS programs use EXEC CICS READ UPDATE / REWRITE for VSAM writes
- VSAM file status checking (WS-RESP-CD / DFHRESP) is the only error handling
- No XA or 2PC support between VSAM and external databases

**Impact**: Data divergence between systems, incorrect rollback decisions, data corruption.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Write to the system of record first (determined by phase), then asynchronously sync to the replica | Backend | Required |
| 2 | Use a reconciliation job that runs hourly to detect and alert on divergence | Backend | Required |
| 3 | Implement a dead-letter queue for failed sync operations with automated retry | Backend | Required |
| 4 | Accept eventual consistency (seconds, not minutes) rather than attempting distributed transactions | Architect | Required |
| 5 | During rollback, replay from the system of record to restore consistency | DevOps | Required |

---

### R-14: Performance Degradation in Migrated Services
**Severity: 4 (Medium)** | **Category: Technical**

**Description**: VSAM provides single-digit millisecond direct-access reads for keyed lookups. PostgreSQL with JPA/Hibernate introduces network hops, connection pooling, ORM overhead, and query planning. Pagination via VSAM BROWSE (STARTBR/READNEXT) is replaced by SQL OFFSET/LIMIT which can be slow for deep pages.

**Evidence from codebase**:
- COTRN00C, COCRDLIC, COUSR00C all use VSAM BROWSE with STARTBR/READNEXT for pagination
- COACTVWC reads from 3 VSAM files (ACCTDAT, CUSTDAT, CARDAIX) for a single account view
- CBTRN02C processes daily files sequentially — I/O bound

**Impact**: Slower response times, degraded user experience, potential SLA violations.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use keyset pagination (`WHERE id > ? ORDER BY id LIMIT ?`) instead of OFFSET/LIMIT for list endpoints | Backend | Required |
| 2 | Add database indexes matching VSAM alternate index access paths (CARDAIX, CXACAIX) | Backend | Required |
| 3 | Use connection pooling (HikariCP) with tuned pool size | Backend | Required |
| 4 | Implement caching (Spring Cache + Redis) for frequently read reference data (discount groups, transaction types) | Backend | Recommended |
| 5 | Set performance baseline from legacy system; target within 120% for P95 latency | QA | Required |

---

### R-15: Mainframe Decommission Timing
**Severity: 3 (Low)** | **Category: Operational**

**Description**: Premature decommission of the mainframe CICS region or VSAM files removes the rollback safety net. Too-late decommission incurs ongoing mainframe licensing costs.

**Impact**: Either loss of rollback capability or unnecessary cost.

**Mitigations**:
| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Keep legacy system available (powered off, not decommissioned) for 90 days after Phase 7 | DevOps | Required |
| 2 | Archive VSAM files to cold storage before decommission | DevOps | Required |
| 3 | Document the legacy restore procedure and test it once before final decommission | DevOps | Required |
| 4 | Negotiate mainframe contract to allow standby/cold-standby pricing during transition | PM | Recommended |

---

## 4. Risk Heat Map

```
           Impact
        Low    Med    High
      ┌──────┬──────┬──────┐
High  │      │ R-03 │ R-07 │
      │      │ R-06 │ R-12 │
Like- ├──────┼──────┼──────┤
li-   │      │ R-04 │ R-01 │
hood  │      │ R-09 │ R-02 │
Med   │      │ R-14 │ R-05 │
      │      │      │ R-08 │
      │      │      │ R-10 │
      │      │      │ R-13 │
      ├──────┼──────┼──────┤
Low   │      │ R-11 │ R-15 │
      └──────┴──────┴──────┘
```

---

## 5. Top-3 Critical Risks Requiring Executive Attention

### 1. R-07: Concurrent Account Balance Updates (Severity 9)
**Why it matters**: Account balance is the most contested shared state. Four separate processes modify it. A lost update means real financial impact.
**Action required**: Approve the architectural decision to use atomic SQL updates and row-level locking. Allocate dedicated testing time for concurrency scenarios.

### 2. R-12: Regression in Validation Rules (Severity 9)
**Why it matters**: 4,237 lines of validation in COACTUPC represent years of accumulated business rules. Missing a rule means invalid data enters production.
**Action required**: Fund a business analyst to catalog every rule before rewriting begins. Approve the characterization testing approach (1000+ test cases).

### 3. R-01/R-10: Financial Precision (Combined Severity 12)
**Why it matters**: COBOL's native packed decimal arithmetic guarantees exact decimal results. Java requires deliberate use of BigDecimal. A single `double` in the calculation chain can introduce rounding errors.
**Action required**: Mandate a code review gate requiring all financial fields to use BigDecimal. Approve 3-month parallel interest calculation before batch cutover.

---

## 6. Risk Monitoring Plan

| Risk | Monitoring Method | Frequency | Alert Threshold |
|------|------------------|-----------|-----------------|
| R-01, R-10 | Parallel calculation comparison | Each batch run | Any penny-level discrepancy |
| R-02 | Record count and checksum validation | Daily during migration | Any count mismatch |
| R-03 | Transaction ID uniqueness check | Real-time | Any duplicate detected |
| R-07 | Balance reconciliation job | Hourly during dual-run | Balance drift > $0.01 |
| R-12 | Characterization test suite | Each deployment | Any test failure |
| R-13 | Dual-write reconciliation job | Hourly | Any record divergence |
| R-14 | APM dashboards (P95 latency) | Real-time | Latency > 120% of baseline |

---

## 7. Risk Ownership

| Role | Risks Owned | Responsibilities |
|------|-------------|-----------------|
| Tech Lead / Architect | R-01, R-03, R-04, R-07, R-10, R-13 | Technical design decisions, code review gates |
| Backend Lead | R-06, R-11, R-12, R-14 | Implementation quality, validation completeness |
| QA Lead | R-02, R-09 | Test coverage, equivalence validation |
| Project Manager | R-05, R-08, R-15 | Scheduling, staffing, stakeholder communication |
| DevOps Lead | R-13, R-15 | Infrastructure, monitoring, rollback procedures |
