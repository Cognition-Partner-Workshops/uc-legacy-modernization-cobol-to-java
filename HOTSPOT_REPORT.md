# Hotspot Report - CardDemo COBOL Codebase

> **Generated**: March 2026  
> **Application**: CardDemo - Mainframe Credit Card Management System  
> **Methodology**: Static analysis of LOC, cyclomatic complexity proxies (EVALUATE/PERFORM), CICS interaction density, file I/O breadth, copybook coupling, and business criticality

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Risk Heat Map](#risk-heat-map)
5. [Modernization Priority Recommendations](#modernization-priority-recommendations)

---

## Scoring Methodology

Each module is scored across five dimensions on a 1-5 scale:

| Dimension            | Weight | Scoring Criteria                                                                |
|---------------------|--------|---------------------------------------------------------------------------------|
| **Code Complexity**  | 25%    | LOC, PERFORM count, EVALUATE count, nesting depth, COPY REPLACING usage         |
| **Data Coupling**    | 20%    | Number of files accessed, copybooks included, cross-entity joins                |
| **Business Impact**  | 25%    | Revenue criticality, user-facing importance, data integrity responsibility       |
| **Migration Risk**   | 20%    | CICS dependencies, assembler calls, ALTER GO TO, complex I/O patterns           |
| **Change Frequency** | 10%    | Estimated change likelihood based on business function centrality               |

**Composite Score** = Weighted sum (max 5.0). Higher score = higher priority for modernization attention.

---

## Top 10 Hotspot Modules

| Rank | Program    | LOC   | Type   | Composite Score | Primary Risk Factor                          |
|------|-----------|-------|--------|-----------------|----------------------------------------------|
| 1    | COACTUPC  | 4,236 | CICS   | **4.70**        | Largest program; 39 COPY REPLACING; 17 CICS  |
| 2    | CBTRN02C  | 731   | Batch  | **4.45**        | Core posting engine; 6 files; financial I-O   |
| 3    | CBSTM03A  | 924   | Batch  | **4.25**        | ALTER GO TO; 13 subprogram calls; I/O heavy   |
| 4    | COCRDLIC  | 1,459 | CICS   | **4.15**        | Complex browse; 18 EVALUATE; 18 CICS commands |
| 5    | COCRDUPC  | 1,560 | CICS   | **4.05**        | Card update with REWRITE; 16 EVALUATE         |
| 6    | CBACT04C  | 652   | Batch  | **4.00**        | Interest calculation; 5 files; financial math  |
| 7    | COTRN02C  | 783   | CICS   | **3.85**        | Transaction add; date validation calls         |
| 8    | COACTVWC  | 941   | CICS   | **3.75**        | Account view; 15 CICS; 14 copybooks           |
| 9    | CBTRN03C  | 649   | Batch  | **3.65**        | Report generation; 6 files; complex formatting |
| 10   | COTRN00C  | 699   | CICS   | **3.55**        | Transaction list; browse logic; 10 CICS        |

---

## Detailed Module Assessments

### 1. COACTUPC - Account Update (Score: 4.70)

**File**: `app/cbl/COACTUPC.cbl` | **LOC**: 4,236 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 4,236  | **Largest program in codebase** (3x average)         |
| PERFORM statements    | 64     | High procedural complexity                           |
| EVALUATE statements   | 10     | Moderate branching logic                             |
| EXEC CICS commands    | 17     | Heavy CICS interaction (READ, REWRITE, SEND, RECEIVE)|
| Copybooks included    | 16+    | Very high coupling (CSSETATY used 39 times via REPLACING) |
| Files accessed        | 3      | ACCTFILE (R/W), CUSTFILE (R/W), CARDXREF (R)         |

**Key Risk Factors**:
- **COPY REPLACING pattern**: 39 instances of `COPY CSSETATY REPLACING` for screen attribute management - extremely verbose, error-prone, and difficult to maintain
- **Dual entity update**: Updates both Account AND Customer records in a single transaction - complex commit/rollback logic
- **Field validation**: Extensive field-by-field validation with individual error messages for every screen field (open date, credit limit, balances, etc.)
- **Size**: At 4,236 lines, this is by far the largest and most complex program, containing business rules for account management

**Modernization Recommendation**: **Decompose** into separate Account Service and Customer Service classes. Replace COPY REPLACING with a reusable validation framework. Extract field validation into a dedicated validator.

---

### 2. CBTRN02C - Transaction Posting Engine (Score: 4.45)

**File**: `app/cbl/CBTRN02C.cbl` | **LOC**: 731 | **Type**: Batch

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 731    | Above average complexity                             |
| PERFORM statements    | 61     | **Highest PERFORM count** in entire codebase         |
| EVALUATE statements   | 0      | Linear flow with conditional logic via IF/PERFORM    |
| Files accessed        | 6      | **Most files of any batch program**: DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF |
| I-O modes             | Mixed  | INPUT, OUTPUT, and I-O (read + rewrite) modes        |

**Key Risk Factors**:
- **Financial core**: This is the **most business-critical batch program** - it posts daily transactions and updates account balances
- **Multi-file I-O with mixed access**: Reads daily transactions, writes to transaction master, updates account balances (I-O), updates category balances (I-O), writes rejected records
- **Data integrity**: A failure mid-run can leave accounts in inconsistent state (partially posted)
- **Reject handling**: Invalid transactions written to DALYREJS - must be reconciled
- **Cross-entity updates**: Updates ACCTFILE (balance) and TCATBALF (category balance) simultaneously

**Modernization Recommendation**: Convert to a Spring Batch job with proper transaction boundaries. Implement database-level ACID transactions to replace the current sequential file processing. Add comprehensive error handling and restart/recovery logic.

---

### 3. CBSTM03A - Statement Generation Driver (Score: 4.25)

**File**: `app/cbl/CBSTM03A.CBL` | **LOC**: 924 | **Type**: Batch

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 924    | High complexity                                      |
| PERFORM statements    | 29     | Moderate procedural flow                             |
| EVALUATE statements   | 5      | Decision logic for output formatting                 |
| CALL statements       | 14     | **13 calls to CBSTM03B** + 1 to CEE3ABD             |
| Files written         | 2      | STMT-FILE (text), HTML-FILE (HTML)                   |

**Key Risk Factors**:
- **ALTER GO TO**: Uses the dangerous `ALTER ... TO PROCEED TO` construct (lines 300-309) to dynamically change paragraph execution flow - this is **the most difficult COBOL construct to convert to modern languages**
- **Dual output format**: Generates both plain text AND HTML statements simultaneously, with extensive WRITE operations (80+ WRITE statements)
- **Tight coupling with CBSTM03B**: The subroutine model uses a shared flag-based protocol (M03B-OPEN/CLOSE/READ/WRITE) rather than clean method signatures
- **Customer-facing output**: Statements are sent to customers - errors have direct reputational impact

**Modernization Recommendation**: Replace ALTER GO TO with a state machine or strategy pattern. Separate text and HTML generation into distinct renderers. Replace the CBSTM03B flag protocol with clean service interfaces. Consider a template engine (Thymeleaf/FreeMarker) for HTML generation.

---

### 4. COCRDLIC - Credit Card List (Score: 4.15)

**File**: `app/cbl/COCRDLIC.cbl` | **LOC**: 1,459 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 1,459  | Second largest CICS program                          |
| PERFORM statements    | 34     | Moderate procedural complexity                       |
| EVALUATE statements   | 18     | **Highest EVALUATE count** - extensive branching     |
| EXEC CICS commands    | 18     | Heavy CICS: STARTBR, READNEXT, ENDBR, SEND, RECEIVE |
| Copybooks included    | 10     | High coupling                                        |

**Key Risk Factors**:
- **Browse/pagination logic**: Implements VSAM BROWSE (STARTBR/READNEXT/ENDBR) for paginated card listing - complex cursor management that must be converted to SQL pagination
- **Dual-mode operation**: Different behavior for admin users (all cards) vs. regular users (filtered by account) - embedded authorization logic
- **18 EVALUATE statements**: Each handling different AID keys (PF keys, Enter, PF3/back, etc.) - complex screen navigation state machine
- **CICS BROWSE state**: Maintains browse position across pseudo-conversational interactions

**Modernization Recommendation**: Replace VSAM browse with JPA/SQL pagination queries. Extract authorization logic into a separate security service. Convert EVALUATE state machine to a controller with proper routing.

---

### 5. COCRDUPC - Credit Card Update (Score: 4.05)

**File**: `app/cbl/COCRDUPC.cbl` | **LOC**: 1,560 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 1,560  | Third largest overall program                        |
| PERFORM statements    | 26     | Moderate complexity                                  |
| EVALUATE statements   | 16     | Heavy branching                                      |
| EXEC CICS commands    | 12     | READ, REWRITE operations                             |
| Files accessed        | 1      | CARDFILE (Read + Rewrite)                            |

**Key Risk Factors**:
- **CICS REWRITE**: Direct VSAM update via EXEC CICS REWRITE - must handle record locking and concurrent access
- **Field-level validation**: Extensive input validation for card fields (number, CVV, expiration, name, status)
- **16 EVALUATE branches**: Complex navigation handling for various PF key combinations
- **COPY CSSTRPFY**: Uses string stripping utility that needs careful conversion

**Modernization Recommendation**: Convert to a REST PUT endpoint with DTO validation (Bean Validation/JSR-380). Replace CICS REWRITE with JPA entity update. Implement optimistic locking for concurrency.

---

### 6. CBACT04C - Interest Calculation (Score: 4.00)

**File**: `app/cbl/CBACT04C.cbl` | **LOC**: 652 | **Type**: Batch

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 652    | Moderate size but high business logic density        |
| PERFORM statements    | 56     | **Second highest** PERFORM count (complex logic)     |
| Files accessed        | 5      | TCATBALF, XREFFILE, DISCGRP, ACCTFILE (I-O), TRANSACT (output) |
| I-O modes             | Mixed  | INPUT + I-O + OUTPUT                                 |

**Key Risk Factors**:
- **Financial calculation**: Interest calculation is **regulatory-sensitive** - must produce identical results after migration
- **Multi-file correlation**: Cross-references 3 input files (category balances, cross-references, disclosure groups) to calculate interest per account
- **Account balance update**: Directly updates account master with new balance after interest calculation
- **Transaction generation**: Creates new transaction records for interest charges
- **56 PERFORM statements**: Dense procedural logic with many subroutine calls

**Modernization Recommendation**: Extract interest calculation into a pure business logic service with comprehensive unit tests. Use BigDecimal for all financial math. Implement parallel testing (COBOL vs. Java) during migration to verify calculation parity. Consider making this a separately deployable microservice due to its regulatory importance.

---

### 7. COTRN02C - Transaction Add (Score: 3.85)

**File**: `app/cbl/COTRN02C.cbl` | **LOC**: 783 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 783    | Above average                                        |
| PERFORM statements    | 61     | **Tied for highest** PERFORM count                   |
| EVALUATE statements   | 13     | Heavy branching                                      |
| EXEC CICS commands    | 11     | Moderate CICS interaction                            |
| External CALLs        | 2      | Calls CSUTLDTC for date validation                   |

**Key Risk Factors**:
- **Transaction creation**: Creates new financial transactions - must maintain data integrity
- **Date validation**: Calls CSUTLDTC utility which in turn calls LE CEEDAYS intrinsic
- **Cross-entity lookup**: Reads account and cross-reference files to validate card/account before creating transaction
- **61 PERFORM statements**: Very high procedural density

**Modernization Recommendation**: Convert to a REST POST endpoint. Replace CSUTLDTC calls with java.time validation. Implement as a transactional service with proper rollback.

---

### 8. COACTVWC - Account View (Score: 3.75)

**File**: `app/cbl/COACTVWC.cbl` | **LOC**: 941 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 941    | Moderate-high                                        |
| PERFORM statements    | 21     | Moderate                                             |
| EVALUATE statements   | 10     | Moderate branching                                   |
| EXEC CICS commands    | 15     | High CICS (3 separate READ operations)               |
| Copybooks included    | 14     | **Highest copybook count** of any single program     |

**Key Risk Factors**:
- **Multi-entity read**: Reads Card (via alternate index), Account, and Customer files in a single screen display
- **14 copybooks**: Extremely high data coupling - changes to any entity structure ripple here
- **Alternate index access**: Uses CICS READ with alternate index (ACCTID) for card lookup - requires careful SQL migration
- **CSSTRPFY string utility**: Embedded string manipulation

**Modernization Recommendation**: Convert to a REST GET endpoint returning a composite DTO. Replace CICS READs with JPA repository calls. Reduce coupling by using a facade pattern over entity services.

---

### 9. CBTRN03C - Transaction Report (Score: 3.65)

**File**: `app/cbl/CBTRN03C.cbl` | **LOC**: 649 | **Type**: Batch

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 649    | Moderate                                             |
| PERFORM statements    | 72     | **Absolute highest** PERFORM count in entire codebase|
| EVALUATE statements   | 4      | Low branching                                        |
| Files accessed        | 6      | TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM, REPTFILE |

**Key Risk Factors**:
- **72 PERFORM statements**: Highest procedural complexity in the codebase - many small paragraphs for report formatting
- **6 file dependencies**: Reads 5 input files plus writes report output
- **Report formatting**: Complex fixed-width report layout with headers, detail lines, page totals, account totals, and grand totals
- **Date parameter file**: Reads external date range parameters - configuration dependency

**Modernization Recommendation**: Replace with a reporting framework (JasperReports, Apache POI). Convert fixed-width output to PDF or HTML. Replace PERFORM-heavy formatting with template-based rendering.

---

### 10. COTRN00C - Transaction List (Score: 3.55)

**File**: `app/cbl/COTRN00C.cbl` | **LOC**: 699 | **Type**: CICS Online

| Metric                | Value  | Assessment                                           |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 699    | Moderate                                             |
| PERFORM statements    | 43     | High                                                 |
| EVALUATE statements   | 8      | Moderate branching                                   |
| EXEC CICS commands    | 10     | Moderate CICS (STARTBR, READNEXT for pagination)     |

**Key Risk Factors**:
- **VSAM browse pagination**: Same browse/cursor pattern as COCRDLIC but for transactions
- **8 EVALUATE branches**: Navigation state handling for different key presses
- **43 PERFORM statements**: Dense procedural flow for list management

**Modernization Recommendation**: Convert to paginated REST GET endpoint with Spring Data JPA Pageable. Replace VSAM browse with SQL `LIMIT/OFFSET` or cursor-based pagination.

---

## Risk Heat Map

### Complexity vs. Business Impact Matrix

```
                        BUSINESS IMPACT
                  Low         Medium        High
              ┌───────────┬───────────┬───────────┐
         High │           │ CBTRN03C  │ COACTUPC  │
              │           │ COCRDLIC  │ CBTRN02C  │
              │           │ COCRDUPC  │ CBSTM03A  │
  COMPLEXITY  │           │           │ CBACT04C  │
              ├───────────┼───────────┼───────────┤
       Medium │ COBSWAIT  │ COACTVWC  │ COTRN02C  │
              │ CBACT02C  │ COTRN00C  │ COBIL00C  │
              │ CBACT03C  │ COUSR00C  │ COSGN00C  │
              ├───────────┼───────────┼───────────┤
         Low  │ UNUSED1Y  │ COMEN01C  │           │
              │ CBCUS01C  │ COADM01C  │           │
              │           │ COUSR01-3C│           │
              └───────────┴───────────┴───────────┘
```

### Migration Difficulty Assessment

| Difficulty Level | Programs                                                     | Key Challenge                         |
|-----------------|--------------------------------------------------------------|---------------------------------------|
| **Very Hard**   | COACTUPC, CBSTM03A                                          | ALTER GO TO, COPY REPLACING, size     |
| **Hard**        | CBTRN02C, CBACT04C, COCRDLIC, COCRDUPC                      | Multi-file I-O, financial logic, BROWSE |
| **Medium**      | COTRN02C, COACTVWC, CBTRN03C, COTRN00C, COCRDSLC, COBIL00C | Standard CICS patterns, moderate size |
| **Easier**      | COSGN00C, COMEN01C, COADM01C, COUSR00-03C, COTRN01C        | Simple CRUD, low complexity           |
| **Straightforward** | CBACT01-03C, CBCUS01C, CBTRN01C, COBSWAIT, CBSTM03B   | Read-only or simple utility           |

---

## Modernization Priority Recommendations

### Phase 1 - Foundation (Weeks 1-4)
**Target**: Low-risk, high-learning-value modules

| Priority | Module     | Rationale                                                    |
|----------|-----------|--------------------------------------------------------------|
| 1.1      | COSGN00C  | Simple, self-contained auth module; establishes security patterns |
| 1.2      | COMEN01C  | Menu routing; establishes navigation/controller pattern      |
| 1.3      | COADM01C  | Mirror of COMEN01C; validates pattern reuse                  |
| 1.4      | COUSR00-03C| Complete CRUD cycle; establishes entity service pattern      |
| 1.5      | Data layer | Convert all copybook layouts to Java POJOs/entities          |

### Phase 2 - Core Read Operations (Weeks 5-8)
**Target**: Read-only modules that exercise data access patterns

| Priority | Module     | Rationale                                                    |
|----------|-----------|--------------------------------------------------------------|
| 2.1      | COACTVWC  | Multi-entity read; establishes composite query pattern       |
| 2.2      | COCRDSLC  | Card detail view; validates entity relationships             |
| 2.3      | COTRN01C  | Transaction view; simple read pattern                        |
| 2.4      | COTRN00C  | Transaction list; establishes pagination pattern             |
| 2.5      | COCRDLIC  | Card list; validates browse-to-pagination conversion         |

### Phase 3 - Write Operations & Business Logic (Weeks 9-14)
**Target**: Programs that modify data - highest risk, highest value

| Priority | Module     | Rationale                                                    |
|----------|-----------|--------------------------------------------------------------|
| 3.1      | COTRN02C  | Transaction creation; first write operation                  |
| 3.2      | COCRDUPC  | Card update; validates REWRITE-to-JPA pattern                |
| 3.3      | COACTUPC  | **Biggest program** - multi-entity update; decompose first   |
| 3.4      | COBIL00C  | Bill payment; financial transaction logic                    |
| 3.5      | CORPT00C  | Report submission; bridges online-to-batch                   |

### Phase 4 - Batch Processing (Weeks 15-20)
**Target**: Batch programs - convert to Spring Batch

| Priority | Module     | Rationale                                                    |
|----------|-----------|--------------------------------------------------------------|
| 4.1      | CBTRN02C  | **Core posting engine** - most critical batch program        |
| 4.2      | CBACT04C  | Interest calculation - requires parallel testing for parity  |
| 4.3      | CBTRN03C  | Transaction report - establish reporting framework           |
| 4.4      | CBSTM03A/B| Statement generation - most complex batch (ALTER GO TO)      |
| 4.5      | CBEXPORT/CBIMPORT | Data exchange - establishes migration utilities       |

### Phase 5 - Utilities & Cleanup (Weeks 21-22)
**Target**: Remaining utilities and integration

| Priority | Module     | Rationale                                                    |
|----------|-----------|--------------------------------------------------------------|
| 5.1      | CSUTLDTC  | Date utility - replace with java.time                        |
| 5.2      | CBACT01-03C| Read/print utilities - convert to diagnostic endpoints      |
| 5.3      | COBSWAIT  | Wait utility - replace with Thread.sleep or scheduler        |
| 5.4      | JCL jobs  | Convert to Spring Batch job configurations and schedulers    |
| 5.5      | Integration testing | End-to-end testing of complete converted system      |

---

## Summary Statistics

| Metric                          | Value                                    |
|---------------------------------|------------------------------------------|
| Total COBOL LOC (core)          | ~18,500 lines                            |
| Avg LOC per program             | ~597 lines                               |
| Max LOC (COACTUPC)              | 4,236 lines                              |
| Total PERFORM statements        | ~750+                                    |
| Total EVALUATE statements       | ~140+                                    |
| Total EXEC CICS commands        | ~170+                                    |
| Programs with file I/O          | 25 of 31                                 |
| Unique VSAM files               | 12                                       |
| Shared copybooks (>5 programs)  | 8                                        |
| Programs calling external modules| 15                                       |
| Estimated migration effort      | 20-22 weeks (5 phases)                   |
