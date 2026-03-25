# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Methodology:** Ranked by composite score of Code Complexity, Migration Risk, and Business Impact

---

## Executive Summary

This report identifies the **top 10 modules** in the CardDemo application that represent the highest complexity, risk, and business impact for a COBOL-to-Java modernization effort. These modules should receive the most attention during architecture design, testing, and migration planning.

**Key Finding:** The top 3 hotspots (COACTUPC, CBTRN02C, CBSTM03A) account for approximately 30% of total codebase complexity and touch the most critical business data (accounts, transactions, statements).

---

## Scoring Methodology

Each module is scored on three dimensions (1-5 scale each):

| Dimension          | Weight | Criteria                                                                      |
|--------------------|--------|-------------------------------------------------------------------------------|
| **Code Complexity**| 35%    | Lines of code, nested logic depth, number of copybooks, EVALUATE/IF nesting, file I/O operations, PERFORM paragraphs |
| **Migration Risk** | 35%    | Shared data coupling, concurrency concerns, CICS/VSAM-specific logic, screen I/O, error handling patterns, PII handling |
| **Business Impact**| 30%    | Revenue criticality, user-facing frequency, data integrity implications, downstream dependencies |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Impact x 0.30), scaled to 100.

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 4,237    | Largest program in the entire codebase                    |
| **Copybooks Used**  | 17       | Most copybook dependencies of any program                 |
| **VSAM Files**      | 5 R/W    | ACCTDAT (R/W), CUSTDAT (R), CCXREF (R), CARDAIX (R), CXACAIX (R) |
| **Complexity Score** | 5/5     | Extensive field-by-field validation, 39 attribute REPLACING directives, CSLKPCDY lookup table (1,318 lines), CSUTLDPY date utilities |
| **Risk Score**       | 5/5     | Shared write to ACCTDAT (also written by batch CBTRN02C/CBACT04C), PII fields, complex BMS screen I/O, abend handling |
| **Impact Score**     | 5/5     | Core account maintenance — balance updates, credit limits, account status changes directly affect financial accuracy |
| **Composite Score**  | **100** |                                                           |

**Migration Concerns:**
- Massive monolithic program — candidate for decomposition into multiple Java services/controllers
- 39 CSSETATY REPLACING directives for BMS field attributes need UI framework mapping
- Date validation via LINK to CSUTLDTC utility — extract as shared service
- CSLKPCDY (1,318-line lookup table) should become database reference data
- Concurrent access with batch programs (CBTRN02C, CBACT04C) requires careful transaction isolation

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 732      | Medium size but high business logic density               |
| **Copybooks Used**  | 6        | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y        |
| **VSAM Files**      | 6 R/W    | DALYTRAN (R), TRANSACT (W), ACCTDAT (R/W), CCXREF (R), TCATBALF (R/W), DALYREJS (W) |
| **Complexity Score** | 4/5     | Multi-file transaction posting with validation, rejection handling, running balance updates |
| **Risk Score**       | 5/5     | Writes to 4 VSAM files atomically — most critical batch job for data integrity. Cross-reference validation. Reject file generation |
| **Impact Score**     | 5/5     | Core daily batch — if this fails, no transactions are posted, balances are wrong, statements are incorrect |
| **Composite Score**  | **93**  |                                                           |

**Migration Concerns:**
- Multi-file atomic updates must map to database transactions with proper rollback
- Sequential file processing (DALYTRAN) maps to Spring Batch reader/processor/writer pattern
- Rejection logic needs comprehensive error handling and dead-letter queue pattern
- Writes to same ACCTDAT/TCATBALF files as CBACT04C — order dependency is critical
- JCL job POSTTRAN is the single execution point — becomes a scheduled Spring Batch job

---

### Rank 3: CBSTM03A — Statement Generation (Batch)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 924      | Plus 230 lines in subroutine CBSTM03B                    |
| **Copybooks Used**  | 5        | CVACT01Y, CVACT03Y, COSTM01, CUSTREC                    |
| **VSAM Files**      | 4 R      | ACCTDAT, CUSTDAT, CCXREF, TRANSACT (all read-only)       |
| **Complexity Score** | 4/5     | Multi-file join logic, SORT integration, HTML output generation, CALL to CBSTM03B subroutine |
| **Risk Score**       | 4/5     | Complex report formatting, HTML generation in COBOL, customer PII in output, GDG management |
| **Impact Score**     | 5/5     | Customer-facing statements — regulatory requirement, directly visible to cardholders |
| **Composite Score**  | **87**  |                                                           |

**Migration Concerns:**
- Two-program structure (CBSTM03A calls CBSTM03B) — consolidate or maintain as service + helper
- HTML generation in COBOL is fragile — replace with proper template engine (Thymeleaf, etc.)
- SORT step integration in JCL (CREASTMT) — becomes in-memory sort or database ORDER BY
- Customer PII (name, address) in output — ensure encryption in transit and at rest
- Uses CUSTREC copybook (slightly different from CVCUS01Y) — normalize during migration

---

### Rank 4: COCRDUPC — Credit Card Update (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 1,560    | Second-largest online program                             |
| **Copybooks Used**  | 13       | Including CVCRD01Y, CSMSG02Y, CSSTRPFY                  |
| **VSAM Files**      | 3 R/W    | CARDDAT (R/W), CUSTDAT (R), CCXREF (R)                  |
| **Complexity Score** | 4/5     | Extensive input validation, BMS screen handling, status change logic |
| **Risk Score**       | 4/5     | Card data updates (CVV, expiration, status) — security-sensitive. PII handling |
| **Impact Score**     | 4/5     | Card maintenance is a frequent user operation. Incorrect updates can block transactions |
| **Composite Score**  | **80**  |                                                           |

**Migration Concerns:**
- Card number handling requires PCI-DSS compliance in modernized application
- CVV code storage/display — must implement tokenization
- BMS screen field validation logic needs mapping to form validation framework
- Shared CVCRD01Y work area with other card programs — ensure consistent DTO usage

---

### Rank 5: COCRDLIC — Credit Card List (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 1,459    | Complex browse/pagination logic                           |
| **Copybooks Used**  | 11       | Including CVCRD01Y for card navigation                   |
| **VSAM Files**      | 2 R      | CARDDAT (R), CCXREF (R)                                  |
| **Complexity Score** | 4/5     | VSAM BROWSE (STARTBR/READNEXT/READPREV) for pagination, dynamic program routing |
| **Risk Score**       | 3/5     | Read-only access reduces write conflict risk. CICS browse logic is VSAM-specific |
| **Impact Score**     | 4/5     | Primary card discovery screen — gateway to card view/update |
| **Composite Score**  | **73**  |                                                           |

**Migration Concerns:**
- VSAM BROWSE (STARTBR/READNEXT/READPREV/ENDBR) maps to database cursor or paginated query
- Forward/backward pagination state management — map to offset/limit or cursor-based pagination
- Dynamic XCTL to COCRDSLC/COCRDUPC based on selection — becomes REST navigation or routing

---

### Rank 6: CBACT04C — Interest Calculation (Batch)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 652      | Moderate size, high financial logic density                |
| **Copybooks Used**  | 5        | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y        |
| **VSAM Files**      | 5 R/W    | ACCTDAT (R/W), CCXREF (R), DISCGRP (R), TCATBALF (R/W), SYSTRAN (W) |
| **Complexity Score** | 4/5     | Interest rate calculation with disclosure group lookup, category balance updates |
| **Risk Score**       | 4/5     | Financial calculation accuracy is critical. Writes to same files as CBTRN02C |
| **Impact Score**     | 5/5     | Revenue-generating — interest charges are core to credit card business model |
| **Composite Score**  | **87** (tied with #3, ranked lower due to lower LOC) |

**Migration Concerns:**
- Financial calculations MUST use BigDecimal in Java — no floating-point rounding errors
- Disclosure group lookup (rate tables) should become database-driven configuration
- Generates synthetic interest transactions into SYSTRAN GDG — needs GDG-to-database mapping
- Order dependency: must run AFTER POSTTRAN and BEFORE TRANBKP

---

### Rank 7: COACTVWC — Account View (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 942      | Moderate size                                             |
| **Copybooks Used**  | 13       | Most diverse file access of any read-only program         |
| **VSAM Files**      | 5 R      | ACCTDAT, CARDDAT, CCXREF, CXACAIX (AIX), CARDAIX (AIX), CUSTDAT |
| **Complexity Score** | 3/5     | Multi-file join via VSAM reads and alternate indexes      |
| **Risk Score**       | 3/5     | Read-only reduces risk, but uses two alternate indexes (CXACAIX, CARDAIX) that need careful DB index mapping |
| **Impact Score**     | 4/5     | Most frequently accessed screen — first menu option for users |
| **Composite Score**  | **67**  |                                                           |

**Migration Concerns:**
- Five VSAM file reads joined in program logic — becomes SQL JOIN in modernized version
- Alternate index access (CXACAIX, CARDAIX) maps to database secondary indexes
- Display-only program — straightforward mapping to read API endpoint + view template
- Serves as the reference implementation for the Account DTO assembly pattern

---

### Rank 8: CBTRN03C — Transaction Report Generation (Batch)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 649      | Moderate size with complex report formatting              |
| **Copybooks Used**  | 5        | CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y        |
| **VSAM Files**      | 4 R      | TRANSACT, CCXREF, TRANTYPE, TRANCATG                    |
| **Complexity Score** | 3/5     | SORT integration, report header/detail/total formatting, page breaks, GDG output |
| **Risk Score**       | 3/5     | Read-only access to files. CVTRA07Y report layout (74 lines) is fragile formatting |
| **Impact Score**     | 4/5     | Daily transaction reports used for reconciliation and audit |
| **Composite Score**  | **67** (tied with #7, ranked lower due to batch-only execution) |

**Migration Concerns:**
- SORT utility integration in JCL must become programmatic sort or database ORDER BY
- CVTRA07Y report formatting layout is column-position based — replace with reporting framework (JasperReports, etc.)
- Date parameter file (DATEPARM) input — becomes configuration property or API parameter
- GDG output for report versioning — map to timestamped file storage or document management

---

### Rank 9: COTRN02C — Transaction Add (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 784      | Moderate size                                             |
| **Copybooks Used**  | 9        | CVACT01Y, CVACT03Y, CVTRA05Y plus standard CICS set     |
| **VSAM Files**      | 4 R/W    | CCXREF (R), CXACAIX (R), ACCTDAT (R), TRANSACT (W)      |
| **Complexity Score** | 3/5     | Input validation, cross-reference lookup, transaction record creation |
| **Risk Score**       | 4/5     | Writes to TRANSACT file that batch also writes to. Validates card-to-account linkage |
| **Impact Score**     | 4/5     | Direct transaction creation — used for manual transaction entry |
| **Composite Score**  | **73** (tied with #5, ranked lower due to fewer file operations) |

**Migration Concerns:**
- Online transaction writes conflict with batch CBTRN02C — need optimistic locking or queue-based approach
- Cross-reference validation (card -> account) via CCXREF and CXACAIX — becomes FK constraint in database
- Transaction timestamp generation uses CICS ASKTIME — replace with Java LocalDateTime.now()
- Amount validation and sign handling — ensure BigDecimal precision

---

### Rank 10: COUSR00C — User List / Security Management (Online CICS)

| Metric              | Value    | Detail                                                    |
|---------------------|----------|-----------------------------------------------------------|
| **Lines of Code**   | 695      | Moderate size                                             |
| **Copybooks Used**  | 8        | CSUSR01Y plus standard CICS set                          |
| **VSAM Files**      | 1 R/W    | USRSEC (R/W — list, select for update/delete)            |
| **Complexity Score** | 3/5     | VSAM BROWSE for user list, selection routing to COUSR02C/COUSR03C |
| **Risk Score**       | 4/5     | Security-critical — manages authentication data. Plain-text passwords in legacy. Admin-only but high consequence |
| **Impact Score**     | 3/5     | Admin function, lower frequency than card/account operations but critical for access control |
| **Composite Score**  | **67**  |                                                           |

**Migration Concerns:**
- Plain-text password storage (SEC-USR-PWD) — MUST implement bcrypt/scrypt hashing
- User type flag ('A'/'U') — map to role-based access control (RBAC) with Spring Security
- VSAM BROWSE pagination — becomes paginated database query
- Gateway to COUSR02C (update) and COUSR03C (delete) — maintain navigation in REST API design
- Consider OAuth2/OIDC integration to replace custom authentication

---

## Hotspot Summary Table

| Rank | Program    | Type   | LOC   | Copybooks | Files R/W | Complexity | Risk | Impact | Score |
|------|------------|--------|-------|-----------|-----------|:----------:|:----:|:------:|:-----:|
| 1    | COACTUPC   | Online | 4,237 | 17        | 5 R/W     | 5          | 5    | 5      | **100** |
| 2    | CBTRN02C   | Batch  | 732   | 6         | 6 R/W     | 4          | 5    | 5      | **93**  |
| 3    | CBSTM03A   | Batch  | 924   | 5         | 4 R       | 4          | 4    | 5      | **87**  |
| 4    | COCRDUPC   | Online | 1,560 | 13        | 3 R/W     | 4          | 4    | 4      | **80**  |
| 5    | COCRDLIC   | Online | 1,459 | 11        | 2 R       | 4          | 3    | 4      | **73**  |
| 6    | CBACT04C   | Batch  | 652   | 5         | 5 R/W     | 4          | 4    | 5      | **87**  |
| 7    | COACTVWC   | Online | 942   | 13        | 5 R       | 3          | 3    | 4      | **67**  |
| 8    | CBTRN03C   | Batch  | 649   | 5         | 4 R       | 3          | 3    | 4      | **67**  |
| 9    | COTRN02C   | Online | 784   | 9         | 4 R/W     | 3          | 4    | 4      | **73**  |
| 10   | COUSR00C   | Online | 695   | 8         | 1 R/W     | 3          | 4    | 3      | **67**  |

---

## Migration Priority Recommendations

### Wave 1 — Highest Priority (Months 1-3)
**Focus:** Core financial accuracy and data integrity

| Module    | Rationale                                                          |
|-----------|--------------------------------------------------------------------|
| CBTRN02C  | Core batch posting — all downstream processing depends on it       |
| CBACT04C  | Interest calculation — revenue-critical, financial precision needed |
| COACTUPC  | Largest program, most dependencies — early conversion de-risks everything downstream |

### Wave 2 — High Priority (Months 3-6)
**Focus:** Customer-facing and statement processing

| Module    | Rationale                                                          |
|-----------|--------------------------------------------------------------------|
| CBSTM03A  | Customer statements — regulatory and customer-visible              |
| COTRN02C  | Online transaction entry — high-frequency user operation           |
| COCRDUPC  | Card updates — PCI-DSS implications need early attention           |

### Wave 3 — Medium Priority (Months 6-9)
**Focus:** Browse/inquiry screens and reporting

| Module    | Rationale                                                          |
|-----------|--------------------------------------------------------------------|
| COCRDLIC  | Card list — complex pagination, but read-only                     |
| COACTVWC  | Account view — reference implementation for read patterns          |
| CBTRN03C  | Transaction reports — audit/compliance requirement                 |

### Wave 4 — Lower Priority (Months 9-12)
**Focus:** Security and admin functions

| Module    | Rationale                                                          |
|-----------|--------------------------------------------------------------------|
| COUSR00C  | User management — important but lower frequency, can use interim auth solution |
| Remaining | Sign-on, menus, utilities — simpler programs, lower risk           |

---

## Cross-Cutting Migration Risks

| Risk Area                    | Affected Programs        | Mitigation Strategy                              |
|------------------------------|--------------------------|--------------------------------------------------|
| **VSAM-to-RDBMS mapping**   | All programs             | Map KSDS to tables with proper indexes, AIX to secondary indexes, GDG to versioned tables |
| **CICS COMMAREA state**     | All 17 online programs   | Replace with session state or JWT token carrying context |
| **BMS screen I/O**          | All 17 online programs   | Map to REST API + React/Angular frontend          |
| **Batch SORT integration**  | CBSTM03A, CBTRN03C      | Use database ORDER BY or Java Collections.sort()  |
| **GDG (Generation Data Groups)** | TRANBKP, COMBTRAN, INTCALC, TRANREPT | Replace with timestamped file storage or database versioning |
| **Plain-text passwords**    | COSGN00C, COUSR00C-03C  | Implement proper hashing (bcrypt) + Spring Security |
| **PII data handling**       | COACTUPC, CBSTM03A, CVCUS01Y | Encryption at rest, masking in logs, GDPR/CCPA compliance |
| **Financial precision**     | CBTRN02C, CBACT04C, COBIL00C | Enforce BigDecimal everywhere, no floating-point for money |
| **Batch window coordination** | CLOSEFIL/OPENFIL + all batch | Replace with database transactions, eliminate batch window requirement |
