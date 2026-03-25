# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Purpose:** Top 10 modules prioritized by complexity, risk, and business impact for modernization planning

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Summary](#top-10-hotspot-summary)
3. [Detailed Hotspot Analysis](#detailed-hotspot-analysis)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Order](#recommended-migration-order)
6. [Cross-Cutting Concerns](#cross-cutting-concerns)

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale each, 30 max):

### Complexity Score (1-10)
| Factor                | Weight | Measurement                                          |
|-----------------------|--------|------------------------------------------------------|
| Lines of Code         | 25%    | >1000=High, 500-1000=Med, <500=Low                  |
| Cyclomatic Complexity | 30%    | IF + EVALUATE count as proxy                         |
| CICS Command Count    | 15%    | Number of EXEC CICS statements                       |
| Copybook Dependencies | 15%    | Number of COPY statements                            |
| CALL Depth            | 15%    | Number of external CALL / XCTL targets               |

### Risk Score (1-10)
| Factor                | Weight | Measurement                                          |
|-----------------------|--------|------------------------------------------------------|
| Data Sensitivity      | 30%    | PII, financial data, credentials handled             |
| File I/O Surface      | 25%    | Number of VSAM files read/written                    |
| Error Handling         | 20%    | HANDLE CONDITION / ABEND presence                    |
| Coupling              | 25%    | Shared COMMAREA fields, cross-program dependencies   |

### Business Impact Score (1-10)
| Factor                | Weight | Measurement                                          |
|-----------------------|--------|------------------------------------------------------|
| Transaction Volume    | 30%    | Estimated runtime frequency / data volume            |
| User-Facing           | 25%    | Online (visible) vs. batch (background)              |
| Revenue Criticality   | 25%    | Payment, billing, interest = high; reports = lower   |
| Downstream Dependencies| 20%   | How many other programs/jobs depend on its output    |

---

## Top 10 Hotspot Summary

| Rank | Module     | LOC  | Type    | Complexity | Risk | Biz Impact | **Total** | Primary Concern                        |
|-----:|------------|-----:|---------|----------:|-----:|-----------:|----------:|----------------------------------------|
|    1 | COACTUPC   | 4236 | Online  |        10 |    9 |          9 |    **28** | Largest program; writes 3 VSAM files   |
|    2 | CBTRN02C   |  731 | Batch   |         8 |    9 |         10 |    **27** | Core transaction posting; balance updates |
|    3 | CBACT04C   |  652 | Batch   |         8 |    9 |         10 |    **27** | Interest calculation; financial engine |
|    4 | CBSTM03A   |  924 | Batch   |         8 |    8 |          9 |    **25** | Statement generation; 13 subprogram calls |
|    5 | COCRDLIC   | 1459 | Online  |         9 |    7 |          8 |    **24** | Card list browse; complex pagination   |
|    6 | COCRDUPC   | 1560 | Online  |         9 |    8 |          7 |    **24** | Card update; validation-heavy          |
|    7 | COBIL00C   |  572 | Online  |         7 |    9 |          8 |    **24** | Bill payment; financial write path     |
|    8 | CBTRN03C   |  649 | Batch   |         8 |    6 |          9 |    **23** | Transaction report; 5 file inputs      |
|    9 | COSGN00C   |  260 | Online  |         4 |   10 |          8 |    **22** | Authentication; plain-text passwords   |
|   10 | CBTRN01C   |  494 | Batch   |         7 |    8 |          7 |    **22** | Daily transaction validation/posting   |

---

## Detailed Hotspot Analysis

### #1: COACTUPC -- Account Update (Score: 28/30)

```
Complexity: 10/10  |  Risk: 9/10  |  Business Impact: 9/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 4,236   | CRITICAL      |
| IF Statements            | 168     | CRITICAL      |
| EVALUATE Statements      | 10      | HIGH          |
| PERFORM Statements       | 64      | HIGH          |
| EXEC CICS Commands       | 17      | HIGH          |
| Copybooks Included       | 56      | CRITICAL      |
| VSAM Files Accessed      | 3 (R/W) | HIGH         |

**Why it's #1:**
- **By far the largest program** at 4,236 LOC -- nearly 3x the next largest online program
- **56 COPY statements** (38 are repeated `CSSETATY` for field attribute setting) -- indicates massive BMS map with many editable fields
- **Writes to 3 VSAM files:** Account (REWRITE), Customer (REWRITE), Card Cross-Reference (READ)
- **168 IF statements** reflect extensive field-by-field validation logic
- Uses `CSUTLDWY` date editing and `CSUTLDPY` date utility procedure copybooks inline
- Contains ABEND handler -- critical error path must be preserved

**Modernization Recommendations:**
- Break into multiple service classes: `AccountValidationService`, `AccountUpdateService`, `AccountDisplayService`
- Extract the 38 `CSSETATY` repetitions into a generic field-attribute loop
- Field validation logic (168 IFs) should become Bean Validation annotations or a validation framework
- Consider splitting the single 4K-line program into a controller + service + repository pattern

---

### #2: CBTRN02C -- Transaction Posting / Master Update (Score: 27/30)

```
Complexity: 8/10  |  Risk: 9/10  |  Business Impact: 10/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 731     | MEDIUM        |
| IF Statements            | 93      | HIGH          |
| PERFORM Statements       | 61      | HIGH          |
| Copybooks Included       | 5       | LOW           |
| VSAM Files Accessed      | 4 (R/W) | HIGH         |

**Why it's #2:**
- **Core financial processing engine** -- posts validated transactions to the master file
- **Updates account balances** (ACCTDATA), transaction category balances (TCATBAL), and the transaction master (TRANSACT)
- Reads Card Cross-Reference (CARDXREF) for account lookup
- **93 IF statements** handle complex business rules: duplicate detection, balance checks, category updates
- Called by JCL POSTTRAN which runs every batch cycle -- **highest frequency batch program**
- Any bug here directly impacts account balances and financial integrity

**Modernization Recommendations:**
- Implement as a Spring Batch `ItemProcessor` with explicit transaction boundaries
- Add idempotency keys to prevent duplicate posting
- Account balance updates should use database transactions with ACID guarantees
- Extensive unit test coverage required -- this is the financial heart of the system

---

### #3: CBACT04C -- Interest Calculation (Score: 27/30)

```
Complexity: 8/10  |  Risk: 9/10  |  Business Impact: 10/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 652     | MEDIUM        |
| IF Statements            | 86      | HIGH          |
| PERFORM Statements       | 56      | HIGH          |
| Copybooks Included       | 5       | LOW           |
| VSAM Files Accessed      | 5 (R/W) | CRITICAL     |

**Why it's #3:**
- **Interest calculation is the most financially sensitive operation** -- errors directly impact revenue
- **Reads 5 files:** Transaction master, Account master, Category balances, Discount groups, Card cross-reference
- **Writes 3 files:** Updates Account balances, Category balances, and creates interest Transaction records
- 86 IF statements implement rate lookup, tier calculations, and rounding rules
- Uses `CVTRA01Y` (category balances) and `CVTRA02Y` (discount/interest rates) for rate determination
- The discount group / interest rate matrix creates complex business logic

**Modernization Recommendations:**
- Implement as a dedicated `InterestCalculationService` with a rules engine or strategy pattern
- Extract rate lookup into a separate `RateService` backed by a database table
- Implement comprehensive audit trail for all interest calculations
- Financial calculations must use `BigDecimal` with explicit rounding modes (HALF_UP)
- Requires parallel-run validation: run old and new simultaneously and compare results

---

### #4: CBSTM03A -- Statement Generation Driver (Score: 25/30)

```
Complexity: 8/10  |  Risk: 8/10  |  Business Impact: 9/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 924     | HIGH          |
| IF Statements            | 15      | LOW           |
| PERFORM Statements       | 29      | MEDIUM        |
| CALL Statements          | 14      | CRITICAL      |
| Copybooks Included       | 4       | LOW           |
| Input Files              | 4       | HIGH          |
| Output Files             | 2       | MEDIUM        |

**Why it's #4:**
- **13 calls to CBSTM03B** (formatting subroutine) -- the tightest coupling in the entire codebase
- Reads 4 input files: sorted transactions (TRXFL), cross-reference (XREFFILE), accounts (ACCTFILE), customers (CUSTFILE)
- Produces both plain-text (PS) and HTML statement output
- Multi-level control break logic: page breaks, account totals, grand totals
- Called by JCL `CREASTMT` which first SORTs and converts transaction data
- Downstream: statement output feeds TXT2PDF1 for PDF generation

**Modernization Recommendations:**
- Replace with a template engine (Thymeleaf, Jasper Reports, or Apache FOP for PDF)
- The 13 CALL/subroutine pattern maps naturally to a `StatementSectionRenderer` interface with implementations
- Consider replacing with an event-driven architecture: transaction events trigger statement line items
- HTML output suggests this is already partially modernized -- build on that

---

### #5: COCRDLIC -- Card List / Browse (Score: 24/30)

```
Complexity: 9/10  |  Risk: 7/10  |  Business Impact: 8/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 1,459   | HIGH          |
| IF Statements            | 122     | CRITICAL      |
| EVALUATE Statements      | 18      | HIGH          |
| PERFORM Statements       | 34      | MEDIUM        |
| EXEC CICS Commands       | 18      | HIGH          |
| Copybooks Included       | 13      | MEDIUM        |

**Why it's #5:**
- **Second-largest online program** with complex forward/backward browse (STARTBR/READNEXT/READPREV/ENDBR)
- **122 IF + 18 EVALUATE** = extremely high cyclomatic complexity
- 3 XCTL targets: returns to menu, drills into card detail (COCRDSLC) or card update (COCRDUPC)
- Complex pagination state management across CICS pseudo-conversational interactions
- Uses both COCRDSL and COCRDLI BMS maps (two mapsets for list vs. selection)

**Modernization Recommendations:**
- Replace VSAM browse with SQL `SELECT ... LIMIT ... OFFSET` pagination
- CICS pseudo-conversational state → REST API with cursor-based pagination
- The 122 IF statements are largely screen field validation -- replace with frontend validation
- Consider combining with COCRDSLC into a single Card service with list/detail endpoints

---

### #6: COCRDUPC -- Card Update (Score: 24/30)

```
Complexity: 9/10  |  Risk: 8/10  |  Business Impact: 7/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 1,560   | HIGH          |
| IF Statements            | 148     | CRITICAL      |
| EVALUATE Statements      | 16      | HIGH          |
| EXEC CICS Commands       | 12      | MEDIUM        |
| Copybooks Included       | 15      | MEDIUM        |

**Why it's #6:**
- **148 IF statements** -- the highest IF count of any online program (field-by-field validation)
- Writes card data back to VSAM (REWRITE) -- data integrity critical
- Reads account, customer, and cross-reference files for context
- Contains ABEND handler for error recovery
- Card number (PAN) handling involves PCI-DSS compliance concerns

**Modernization Recommendations:**
- Extract validation into a `CardValidationService` with Bean Validation
- Card update operations need audit logging for PCI-DSS compliance
- Field-level validation (148 IFs) should become declarative annotations
- Consider using optimistic locking instead of CICS record-level locking

---

### #7: COBIL00C -- Bill Payment (Score: 24/30)

```
Complexity: 7/10  |  Risk: 9/10  |  Business Impact: 8/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 572     | MEDIUM        |
| IF Statements            | 10      | LOW           |
| EVALUATE Statements      | 9       | MEDIUM        |
| EXEC CICS Commands       | 13      | HIGH          |
| VSAM Files Accessed      | 3 (R/W) | HIGH         |

**Why it's #7:**
- **Financial write path** -- creates payment transactions and updates account balances
- Reads account data (ACCTDAT), cross-reference (CXACAIX alternate index), and transaction file
- **Writes to TRANSACT** (new payment transaction) and **REWRITEs ACCTDAT** (balance update)
- Uses ASKTIME/FORMATTIME for timestamp generation -- timestamp handling is critical
- Despite moderate LOC, the financial risk is extremely high

**Modernization Recommendations:**
- Must be wrapped in a database transaction with rollback capability
- Payment idempotency is critical -- implement idempotency keys
- Add real-time balance validation before payment acceptance
- Consider async processing with confirmation for large payments

---

### #8: CBTRN03C -- Transaction Report Generation (Score: 23/30)

```
Complexity: 8/10  |  Risk: 6/10  |  Business Impact: 9/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 649     | MEDIUM        |
| IF Statements            | 75      | HIGH          |
| PERFORM Statements       | 72      | HIGH          |
| Copybooks Included       | 5       | LOW           |
| Input Files              | 5       | HIGH          |

**Why it's #8:**
- **Reads 5 input files:** Transaction, Cross-reference, Transaction types, Transaction categories, Date parameters
- 72 PERFORMs and 75 IFs implement multi-level control break report logic
- Uses `CVTRA07Y` report layout copybook with headers, detail lines, page/account/grand totals
- Output feeds the TRANREPT GDG (Generation Data Group) -- rolling report archive
- Report date range parameterized via DATEPARM file

**Modernization Recommendations:**
- Replace with a reporting framework (JasperReports, BIRT) or SQL-based reporting
- Control break logic maps to SQL `GROUP BY` with `ROLLUP`
- GDG archive pattern maps to date-partitioned tables or object storage
- Consider real-time dashboard instead of batch report generation

---

### #9: COSGN00C -- Sign-On / Authentication (Score: 22/30)

```
Complexity: 4/10  |  Risk: 10/10  |  Business Impact: 8/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 260     | LOW           |
| IF Statements            | 4       | LOW           |
| EXEC CICS Commands       | 10      | MEDIUM        |
| VSAM Files Accessed      | 1 (R)   | LOW           |

**Why it's #9 despite low complexity:**
- **Maximum risk score (10/10)** due to:
  - **Plain-text password storage** in USRSEC VSAM file (CSUSR01Y: `SEC-USR-PWD PIC X(08)`)
  - **No password hashing, salting, or encryption**
  - **No account lockout** after failed attempts
  - **No session timeout** management
  - **No MFA/2FA** support
  - **8-character password limit** (PIC X(08))
- Single gateway to the entire application -- compromise here = full system compromise
- Routes users to admin menu (COADM01C) or regular menu (COMEN01C) based on user type

**Modernization Recommendations:**
- **CRITICAL PRIORITY:** Replace with proper authentication (Spring Security, OAuth2/OIDC)
- Implement bcrypt/scrypt password hashing
- Add account lockout policy (e.g., 5 failed attempts)
- Implement session management with configurable timeouts
- Add MFA support
- Replace the 8-char password limit with modern password policy

---

### #10: CBTRN01C -- Daily Transaction Validation/Posting (Score: 22/30)

```
Complexity: 7/10  |  Risk: 8/10  |  Business Impact: 7/10
```

| Metric                   | Value   | Concern Level |
|--------------------------|---------|:-------------:|
| Lines of Code            | 494     | MEDIUM        |
| IF Statements            | 33      | MEDIUM        |
| PERFORM Statements       | 42      | MEDIUM        |
| Copybooks Included       | 6       | LOW           |
| VSAM Files Accessed      | 4 (R)   | HIGH          |

**Why it's #10:**
- **First step in the batch posting pipeline** -- validates incoming daily transactions
- Reads daily transactions (DALYTRAN), looks up cards (CVACT02Y), accounts (CVACT01Y), cross-references (CVACT03Y), and customers (CVCUS01Y)
- Validation failures here cascade to all downstream processing
- Feeds validated transactions to CBTRN02C for master file update

**Modernization Recommendations:**
- Implement as a Spring Batch `ItemReader` + `ItemProcessor` with skip/retry policies
- Add dead-letter queue for rejected transactions instead of file-based rejects
- Validation rules should be externalized (configurable business rules)
- Add real-time validation option as an alternative to batch-only processing

---

## Risk Heat Map

```
                    LOW Risk ◄──────────────────────► HIGH Risk
                    │                                        │
HIGH Complexity     │  COCRDLIC ·····  COCRDUPC              │
                    │  CBTRN03C       COACTUPC ★★★           │
                    │                 CBSTM03A               │
                    │                                        │
                    │                                        │
MED Complexity      │  COTRN00C       CBTRN02C ★★            │
                    │  COUSR00C       CBACT04C ★★            │
                    │  CBTRN01C       COBIL00C               │
                    │                                        │
                    │                                        │
LOW Complexity      │  COUSR01C       COSGN00C ★★★           │
                    │  COTRN01C                              │
                    │  COMEN01C                              │
                    │                                        │

★★★ = Migrate First (high risk regardless of complexity)
★★  = Migrate Second (high risk + medium/high complexity)
```

---

## Recommended Migration Order

Based on the hotspot analysis, the recommended migration order balances risk reduction with incremental delivery:

### Wave 1: Security Foundation (Weeks 1-3)
| Priority | Module    | Rationale                                         |
|----------|-----------|---------------------------------------------------|
| 1        | COSGN00C  | Eliminate plain-text password vulnerability        |
| 2        | CSUSR01Y  | Redesign user security model (RBAC, hashing)      |
| 3        | COUSR00-03C| User CRUD depends on new security model          |

### Wave 2: Financial Core (Weeks 4-8)
| Priority | Module    | Rationale                                         |
|----------|-----------|---------------------------------------------------|
| 4        | CBTRN02C  | Core posting engine -- highest business impact    |
| 5        | CBACT04C  | Interest calculation -- revenue critical          |
| 6        | COBIL00C  | Bill payment -- financial write path              |
| 7        | CBTRN01C  | Daily validation feeds posting engine             |

### Wave 3: Customer Experience (Weeks 9-14)
| Priority | Module    | Rationale                                         |
|----------|-----------|---------------------------------------------------|
| 8        | COACTUPC  | Largest program -- break into microservices       |
| 9        | COCRDLIC  | Card browse -- high complexity, user-facing       |
| 10       | COCRDUPC  | Card update -- validation-heavy                   |

### Wave 4: Reporting & Statements (Weeks 15-18)
| Priority | Module    | Rationale                                         |
|----------|-----------|---------------------------------------------------|
| 11       | CBSTM03A/B| Replace with modern template engine              |
| 12       | CBTRN03C  | Replace with SQL-based reporting                  |
| 13       | CORPT00C  | Online report request UI                          |

### Wave 5: Remaining Programs & Infrastructure (Weeks 19-24)
| Priority | Module    | Rationale                                         |
|----------|-----------|---------------------------------------------------|
| 14       | COACTVWC  | Account view (read-only, lower risk)              |
| 15       | COCRDSLC  | Card detail view                                  |
| 16       | COTRN00-02C| Transaction list/view/add                        |
| 17       | COMEN01C/COADM01C | Menu navigation → web routing            |
| 18       | Data loaders| CBACT01-03C, CBCUS01C → DB migration scripts   |
| 19       | CBEXPORT/CBIMPORT | Replace with ETL or API integration      |
| 20       | JCL jobs  | Replace with Spring Batch / scheduler             |

---

## Cross-Cutting Concerns

These issues affect multiple hotspot modules and should be addressed as shared infrastructure:

### 1. Date/Time Handling (Affects: ALL programs)
- **6 copybooks** dedicated to date handling: CSDAT01Y, CSUTLDWY, CSUTLDPY, CODATECN, plus ASM routine COBDATFT
- **Action:** Replace with `java.time` API; create a shared `DateTimeService`

### 2. Error Handling (Affects: ALL batch programs)
- All batch programs call `CEE3ABD` for abnormal termination
- Online programs use `EXEC CICS HANDLE CONDITION` / `EXEC CICS ABEND`
- **Action:** Implement a global exception handler with structured logging

### 3. COMMAREA / Session State (Affects: ALL online programs)
- 17 online programs share `COCOM01Y` communication area
- Navigation state, user context, and entity keys passed between programs
- **Action:** Replace with HTTP session / JWT tokens / Spring Security context

### 4. BMS Screen Field Attributes (Affects: COACTUPC and other update screens)
- `CSSETATY` copybook repeated 38+ times in COACTUPC for field attribute control
- **Action:** Replace with CSS classes and frontend validation framework

### 5. VSAM File I/O Patterns (Affects: ALL programs)
- CICS: READ/REWRITE/WRITE/DELETE/STARTBR/READNEXT/READPREV/ENDBR
- Batch: Sequential READ/WRITE with file status checking
- **Action:** Replace with JPA/Hibernate repositories; batch I/O with Spring Batch readers/writers

### 6. Lookup Tables (Affects: CSLKPCDY -- 1,318 lines)
- US phone area codes, state codes, and ZIP code validation hardcoded in COBOL
- **Action:** Replace with database reference tables or external validation service

### 7. PCI-DSS Compliance (Affects: Card-handling programs)
- Card numbers (PAN) stored and transmitted in clear text
- CVV codes stored in VSAM
- **Action:** Implement tokenization, encryption at rest, and mask display values
